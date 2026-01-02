import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import apiFacade from '../util/apiFacade';
import { ROUTES } from '../utils/routes';
import { formatDateTime } from '../utils/dateFormatter';
import {
  PageContainer,
  PageHeader,
  PageTitle,
  PageSubtitle,
  ContentCard,
  CardHeader,
  CardTitle,
  CardContent,
  PaymentHistoryItem,
  PaymentInfo,
  PaymentDescription,
  PaymentDate,
  PaymentAmount,
  StatusBadge,
  EmptyState,
  LoadingSpinner,
  ErrorMessage,
  FilterSection,
  FilterButton,
  ViewReceiptButton,
} from './Payments.styles';

const Payments = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [payments, setPayments] = useState([]);
  const [filteredPayments, setFilteredPayments] = useState([]);
  const [filter, setFilter] = useState('all'); // all, completed, pending, failed
  const [error, setError] = useState('');

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    fetchPayments();
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    // Filter payments whenever filter or payments change
    if (filter === 'all') {
      setFilteredPayments(payments);
    } else {
      setFilteredPayments(payments.filter(p => p.status?.toLowerCase() === filter));
    }
  }, [filter, payments]);

  const fetchPayments = async () => {
    try {
      setLoading(true);
      setError('');
      const customerId = apiFacade.getCustomerId();
      
      if (!customerId) {
        setError('Customer ID not found');
        return;
      }

      // Fetch completed payments from receipts
      const receipts = await apiFacade.getCustomerReceipts(customerId);
      
      // Transform receipts to payment format
      const completedPayments = receipts?.map(receipt => ({
        id: receipt.id,
        amount: receipt.priceCents,
        currency: 'DKK',
        status: receipt.status === 'PAID' ? 'COMPLETED' : receipt.status,
        description: receipt.pmBrand ? 
          `${receipt.pmBrand} ending in ${receipt.pmLast4}` : 
          'Payment',
        createdAt: receipt.createdAt,
        receiptId: receipt.id,
        receiptNumber: receipt.receiptNumber
      })) || [];

      // Fetch subscription to check for pending payments
      try {
        const subData = await apiFacade.getCustomerSubscription(customerId);
        
        if (subData?.planId) {
          const planData = await apiFacade.getPlanById(subData.planId);
          
          // Check if subscription payment is due
          if (subData.nextBillingDate && planData) {
            const today = new Date();
            const billingDate = new Date(subData.nextBillingDate);
            
            if (billingDate <= today) {
              // Add pending subscription payment
              const pendingPayment = {
                id: `pending-${subData.id}`,
                amount: planData.priceCents,
                currency: planData.currency,
                status: 'PENDING',
                description: `Subscription Payment: ${planData.name}`,
                createdAt: subData.nextBillingDate,
                isPending: true,
                subscriptionId: subData.id
              };
              
              completedPayments.unshift(pendingPayment); // Add to beginning
            }
          }
        }
      } catch (err) {
        console.log('No subscription or subscription data unavailable:', err);
      }
      
      setPayments(completedPayments);
      console.log('Payments loaded:', completedPayments.length);
    } catch (err) {
      console.error('Error fetching payments:', err);
      setError('Failed to load payment history');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amountCents, currency = 'DKK') => {
    if (!amountCents) return '0.00 DKK';
    return `${(amountCents / 100).toFixed(2)} ${currency}`;
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'completed':
      case 'paid':
        return 'success';
      case 'pending':
        return 'pending';
      case 'failed':
      case 'cancelled':
        return 'error';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <PageContainer>
        <LoadingSpinner>Loading payment history...</LoadingSpinner>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <PageHeader>
        <div>
          <PageTitle>Payment History</PageTitle>
          <PageSubtitle>View all your transactions and receipts</PageSubtitle>
        </div>
      </PageHeader>

      {error && <ErrorMessage>{error}</ErrorMessage>}

      <FilterSection>
        <FilterButton 
          active={filter === 'all'} 
          onClick={() => setFilter('all')}
        >
          All ({payments.length})
        </FilterButton>
        <FilterButton 
          active={filter === 'completed'} 
          onClick={() => setFilter('completed')}
        >
          Completed ({payments.filter(p => p.status?.toLowerCase() === 'completed').length})
        </FilterButton>
        <FilterButton 
          active={filter === 'pending'} 
          onClick={() => setFilter('pending')}
        >
          Pending ({payments.filter(p => p.status?.toLowerCase() === 'pending').length})
        </FilterButton>
        <FilterButton 
          active={filter === 'failed'} 
          onClick={() => setFilter('failed')}
        >
          Failed ({payments.filter(p => p.status?.toLowerCase() === 'failed').length})
        </FilterButton>
      </FilterSection>

      <ContentCard>
        <CardHeader>
          <CardTitle>Transactions</CardTitle>
        </CardHeader>
        <CardContent>
          {filteredPayments.length === 0 ? (
            <EmptyState>
              <p>No {filter !== 'all' ? filter : ''} payments found</p>
              <Link to={ROUTES.buySMS}>
                <ViewReceiptButton style={{ marginTop: '1rem' }}>
                  Make a Purchase
                </ViewReceiptButton>
              </Link>
            </EmptyState>
          ) : (
            filteredPayments.map((payment) => (
              <PaymentHistoryItem key={payment.id}>
                <PaymentInfo>
                  <PaymentDescription>
                    {payment.description}
                    {payment.receiptNumber && (
                      <span style={{ 
                        marginLeft: '8px', 
                        fontSize: '0.875rem', 
                        color: '#718096' 
                      }}>
                        #{payment.receiptNumber}
                      </span>
                    )}
                  </PaymentDescription>
                  <PaymentDate>{formatDateTime(payment.createdAt)}</PaymentDate>
                </PaymentInfo>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <PaymentAmount>
                    {formatCurrency(payment.amount, payment.currency)}
                  </PaymentAmount>
                  <StatusBadge status={getStatusColor(payment.status)}>
                    {payment.status}
                  </StatusBadge>
                  {payment.isPending ? (
                    <Link to={ROUTES.paySubscription}>
                      <ViewReceiptButton style={{ 
                        backgroundColor: '#e53e3e',
                        color: 'white',
                        fontWeight: 'bold'
                      }}>
                        Pay Now
                      </ViewReceiptButton>
                    </Link>
                  ) : payment.receiptId && (
                    <ViewReceiptButton onClick={() => {
                      // Navigate to receipt view or download
                      console.log('View receipt:', payment.receiptId);
                    }}>
                      View Receipt
                    </ViewReceiptButton>
                  )}
                </div>
              </PaymentHistoryItem>
            ))
          )}
        </CardContent>
      </ContentCard>

      <div style={{ marginTop: '2rem', textAlign: 'center' }}>
        <p style={{ color: '#718096', marginBottom: '1rem' }}>
          Need to make a payment?
        </p>
        <Link to={ROUTES.buySMS}>
          <ViewReceiptButton style={{ padding: '0.75rem 2rem', fontSize: '1rem' }}>
            Buy SMS Credits
          </ViewReceiptButton>
        </Link>
      </div>
    </PageContainer>
  );
};

export default Payments;
