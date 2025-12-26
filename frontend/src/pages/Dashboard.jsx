import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiFacade from '../util/apiFacade';
import { useAuth } from '../hooks/useAuth';
import { ROUTES } from '../utils/routes';
import {
  DashboardContainer,
  DashboardHeader,
  WelcomeText,
  SubText,
  DashboardGrid,
  Card,
  LargeCard,
  CardHeader,
  CardTitle,
  CardIcon,
  CardContent,
  InfoRow,
  InfoLabel,
  InfoValue,
  StatusBadge,
  DropdownButton,
  DropdownContent,
  DropdownItem,
  EmptyState,
  LoadingSpinner,
  ErrorMessage,
  ActivityItem,
  ActivityType,
  ActivityDate,
  PaymentItem,
  PaymentAmount,
  PaymentDate,
  ReceiptItem,
  ViewButton,
} from './Dashboard.styles';

const Dashboard = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [customerData, setCustomerData] = useState(null);
  const [subscription, setSubscription] = useState(null);
  const [smsBalance, setSmsBalance] = useState(null);
  const [payments, setPayments] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [activities, setActivities] = useState([]);
  const [receipts, setReceipts] = useState([]);
  const [error, setError] = useState('');
  const [showSubscriptionDetails, setShowSubscriptionDetails] = useState(false);

  useEffect(() => {
    // Redirect to login if not authenticated
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    // Fetch dashboard data
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        const customerId = apiFacade.getCustomerId();
        
        if (customerId) {
          // Fetch customer profile
          const customerProfile = await apiFacade.getCustomerProfile(customerId);
          setCustomerData(customerProfile);
          
          // Fetch subscription data
          try {
            const subscriptionData = await apiFacade.getCustomerSubscription(customerId);
            setSubscription(subscriptionData);
          } catch (err) {
            console.log('No subscription data available');
          }
          
          // Fetch SMS balance
          try {
            const balance = await apiFacade.getSmsBalance(customerId);
            setSmsBalance(balance);
          } catch (err) {
            console.log('No SMS balance data available:', err);
          }

          // Fetch payment methods
          try {
            const paymentMethodData = await apiFacade.getCustomerPaymentMethods(customerId);
            setPaymentMethods(paymentMethodData || []);
          } catch (err) {
            console.log('No payment methods available:', err);
          }

          // Fetch receipts
          try {
            const receiptData = await apiFacade.getCustomerReceipts(customerId);
            setReceipts(receiptData || []);
            
            // Create mock payments from receipts if available
            if (receiptData && receiptData.length > 0) {
              const mockPayments = receiptData.map(receipt => ({
                id: receipt.id,
                amount: receipt.amount,
                description: receipt.description || 'Payment',
                createdAt: receipt.createdAt
              }));
              setPayments(mockPayments);
            }
          } catch (err) {
            console.log('No receipts available:', err);
          }

          // Create mock activity data
          const mockActivities = [
            { type: 'LOGIN', createdAt: new Date().toISOString(), status: 'SUCCESS' }
          ];
          
          if (subscription) {
            mockActivities.push({
              type: 'SUBSCRIPTION_CREATED',
              createdAt: subscription.startDate || new Date().toISOString(),
              status: 'SUCCESS'
            });
          }
          
          setActivities(mockActivities);
        }
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try refreshing the page.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [isAuthenticated, navigate]);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('da-DK', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatCurrency = (amount) => {
    if (!amount) return '0 kr';
    return `${amount.toFixed(2)} kr`;
  };

  const formatActivityType = (type) => {
    const typeMap = {
      'LOGIN': 'Logged in',
      'LOGOUT': 'Logged out',
      'SUBSCRIPTION_CREATED': 'Subscription created',
      'SUBSCRIPTION_UPDATED': 'Subscription updated',
      'SUBSCRIPTION_CANCELLED': 'Subscription cancelled',
      'PAYMENT_SUCCESS': 'Payment successful',
      'PAYMENT_FAILED': 'Payment failed',
      'SMS_PURCHASED': 'SMS credits purchased',
      'PROFILE_UPDATED': 'Profile updated'
    };
    return typeMap[type] || type;
  };

  if (loading) {
    return (
      <DashboardContainer>
        <LoadingSpinner>Loading your dashboard...</LoadingSpinner>
      </DashboardContainer>
    );
  }

  if (error) {
    return (
      <DashboardContainer>
        <ErrorMessage>{error}</ErrorMessage>
      </DashboardContainer>
    );
  }

  return (
    <DashboardContainer>
      <DashboardHeader>
        <WelcomeText>Hej, {customerData?.companyName || 'Velkommen'}!</WelcomeText>
        <SubText>{user?.email || ''}</SubText>
      </DashboardHeader>

      <DashboardGrid>
        {/* Profile Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              <CardIcon>👤</CardIcon>
              Profile
            </CardTitle>
          </CardHeader>
          <CardContent>
            {customerData ? (
              <>
                <InfoRow>
                  <InfoLabel>Company</InfoLabel>
                  <InfoValue>{customerData.companyName}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Email</InfoLabel>
                  <InfoValue>{user?.email}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Serial Number</InfoLabel>
                  <InfoValue>{customerData.serialNumber}</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Customer ID</InfoLabel>
                  <InfoValue>#{customerData.id}</InfoValue>
                </InfoRow>
              </>
            ) : (
              <EmptyState>No profile data available</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Abonnement Card with Dropdown */}
        <Card>
          <CardHeader>
            <CardTitle>
              <CardIcon>📋</CardIcon>
              Abonnement
            </CardTitle>
          </CardHeader>
          <CardContent>
            {subscription ? (
              <>
                <DropdownButton onClick={() => setShowSubscriptionDetails(!showSubscriptionDetails)}>
                  <span>{subscription.plan?.name || 'No Plan'}</span>
                  <span>{showSubscriptionDetails ? '▲' : '▼'}</span>
                </DropdownButton>
                
                {showSubscriptionDetails && (
                  <DropdownContent>
                    <DropdownItem>
                      <InfoRow>
                        <InfoLabel>Status</InfoLabel>
                        <StatusBadge status={subscription.status}>
                          {subscription.status}
                        </StatusBadge>
                      </InfoRow>
                    </DropdownItem>
                    <DropdownItem>
                      <InfoRow>
                        <InfoLabel>Start Date</InfoLabel>
                        <InfoValue>{formatDate(subscription.startDate)}</InfoValue>
                      </InfoRow>
                    </DropdownItem>
                    <DropdownItem>
                      <InfoRow>
                        <InfoLabel>Next Billing</InfoLabel>
                        <InfoValue>{formatDate(subscription.nextBillingDate)}</InfoValue>
                      </InfoRow>
                    </DropdownItem>
                    <DropdownItem>
                      <InfoRow>
                        <InfoLabel>Plan ID</InfoLabel>
                        <InfoValue>#{subscription.plan?.id}</InfoValue>
                      </InfoRow>
                    </DropdownItem>
                  </DropdownContent>
                )}
              </>
            ) : (
              <EmptyState>No active subscription</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* SMS Balance Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              <CardIcon>💬</CardIcon>
              SMS Balance
            </CardTitle>
          </CardHeader>
          <CardContent>
            {smsBalance ? (
              <>
                <InfoRow>
                  <InfoLabel>Remaining</InfoLabel>
                  <InfoValue style={{ color: '#6BB8E8', fontSize: '1.5rem' }}>
                    {smsBalance.remainingSmsCredits || 0}
                  </InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Total Used</InfoLabel>
                  <InfoValue>{smsBalance.usedSmsCredits || 0} SMS</InfoValue>
                </InfoRow>
                <InfoRow>
                  <InfoLabel>Last Recharged</InfoLabel>
                  <InfoValue>{formatDate(smsBalance.lastRechargedAt)}</InfoValue>
                </InfoRow>
              </>
            ) : (
              <EmptyState>No SMS balance data</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Betalinger (Payment Methods) Card */}
        <LargeCard>
          <CardHeader>
            <CardTitle>
              <CardIcon>💳</CardIcon>
              Betalingsmetoder
            </CardTitle>
          </CardHeader>
          <CardContent>
            {paymentMethods && paymentMethods.length > 0 ? (
              paymentMethods.map((method, index) => (
                <PaymentItem key={index}>
                  <div>
                    <div style={{ fontWeight: 600, marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      {method.brand && (
                        <span style={{ textTransform: 'capitalize' }}>{method.brand}</span>
                      )}
                      <span>•••• {method.last4}</span>
                      {method.isDefault && (
                        <StatusBadge status="ACTIVE" style={{ fontSize: '0.65rem', padding: '2px 8px' }}>
                          Default
                        </StatusBadge>
                      )}
                    </div>
                    <PaymentDate>
                      Expires {method.expMonth}/{method.expYear}
                    </PaymentDate>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
                    <StatusBadge status={method.status}>
                      {method.status}
                    </StatusBadge>
                    <span style={{ fontSize: '0.75rem', color: '#718096', textTransform: 'capitalize' }}>
                      {method.type}
                    </span>
                  </div>
                </PaymentItem>
              ))
            ) : (
              <EmptyState>No payment methods saved</EmptyState>
            )}
          </CardContent>
        </LargeCard>

        {/* Aktiviteter (Activities) Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              <CardIcon>📊</CardIcon>
              Aktiviteter
            </CardTitle>
          </CardHeader>
          <CardContent>
            {activities && activities.length > 0 ? (
              activities.slice(0, 5).map((activity, index) => (
                <ActivityItem key={index}>
                  <ActivityType>{formatActivityType(activity.type)}</ActivityType>
                  <ActivityDate>
                    {formatDate(activity.createdAt)}
                    {activity.status && ` • ${activity.status}`}
                  </ActivityDate>
                </ActivityItem>
              ))
            ) : (
              <EmptyState>No recent activities</EmptyState>
            )}
          </CardContent>
        </Card>

        {/* Kvitteringer (Receipts) Card */}
        <Card>
          <CardHeader>
            <CardTitle>
              <CardIcon>🧾</CardIcon>
              Kvitteringer
            </CardTitle>
          </CardHeader>
          <CardContent>
            {receipts && receipts.length > 0 ? (
              receipts.slice(0, 5).map((receipt, index) => (
                <ReceiptItem key={index}>
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '0.875rem', marginBottom: '4px' }}>
                      Receipt #{receipt.id}
                    </div>
                    <PaymentDate>{formatDate(receipt.createdAt)}</PaymentDate>
                  </div>
                  <ViewButton>View</ViewButton>
                </ReceiptItem>
              ))
            ) : (
              <EmptyState>No receipts available</EmptyState>
            )}
          </CardContent>
        </Card>
      </DashboardGrid>
    </DashboardContainer>
  );
};

export default Dashboard;
