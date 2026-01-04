import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import apiFacade from '../util/apiFacade';
import { ROUTES } from '../utils/routes';
import {
  PageContainer,
  PageHeader,
  PageTitle,
  PageSubtitle,
  ContentCard,
  CardHeader,
  CardTitle,
  CardContent,
  PaymentMethodCard,
  PaymentMethodInfo,
  PaymentMethodBrand,
  PaymentMethodNumber,
  PaymentMethodExpiry,
  DefaultBadge,
  StatusBadge,
  AddCardButton,
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalTitle,
  ModalBody,
  ModalFooter,
  FormGroup,
  Label,
  Input,
  Checkbox,
  CheckboxLabel,
  Button,
  ButtonSecondary,
  EmptyState,
  LoadingSpinner,
  ErrorMessage,
  SuccessMessage,
} from './PaymentMethods.styles';

const PaymentMethods = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Form state
  const [cardNumber, setCardNumber] = useState('');
  const [expMonth, setExpMonth] = useState('');
  const [expYear, setExpYear] = useState('');
  const [cvc, setCvc] = useState('');
  const [isDefault, setIsDefault] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    fetchPaymentMethods();
  }, [isAuthenticated, navigate]);

  const fetchPaymentMethods = async () => {
    try {
      setLoading(true);
      const customerId = apiFacade.getCustomerId();
      
      if (customerId) {
        const methods = await apiFacade.getCustomerPaymentMethods(customerId);
        setPaymentMethods(methods || []);
      }
    } catch (err) {
      console.error('Error fetching payment methods:', err);
      setError('Failed to load payment methods');
    } finally {
      setLoading(false);
    }
  };

  const handleAddCard = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setSubmitting(true);

    try {
      const customerId = apiFacade.getCustomerId();
      
      console.log('Adding payment method...');
      console.log('Customer ID:', customerId);
      
      if (!customerId) {
        throw new Error('Customer ID not found. Please log in again.');
      }

      const paymentMethodData = {
        customerId: parseInt(customerId),
        cardNumber,
        expMonth: parseInt(expMonth),
        expYear: parseInt(expYear),
        cvc,
        isDefault
      };
      
      console.log('Payment method data:', { ...paymentMethodData, cardNumber: '****', cvc: '***' });
      
      const response = await apiFacade.addPaymentMethod(paymentMethodData);
      
      console.log('Payment method added:', response);
      setSuccess('Payment method added successfully!');
      
      // Reset form
      setCardNumber('');
      setExpMonth('');
      setExpYear('');
      setCvc('');
      setIsDefault(false);
      
      // Refresh payment methods
      await fetchPaymentMethods();
      
      // Close modal after a short delay
      setTimeout(() => {
        setShowAddModal(false);
        setSuccess('');
      }, 2000);

    } catch (err) {
      console.error('Error adding payment method:', err);
      console.error('Error details:', err);
      
      // Handle different error types
      if (err.fullError) {
        try {
          const errorDetails = await err.fullError;
          console.error('Backend error:', errorDetails);
          setError(errorDetails.msg || 'Failed to add payment method');
        } catch {
          setError('Failed to add payment method. Please check your details.');
        }
      } else {
        setError(err.message || 'Failed to add payment method. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const formatCardNumber = (value) => {
    // Remove all non-digit characters
    const cleaned = value.replace(/\D/g, '');
    // Add spaces every 4 digits
    const formatted = cleaned.match(/.{1,4}/g)?.join(' ') || cleaned;
    return formatted.substring(0, 19); // Max 16 digits + 3 spaces
  };

  const handleCardNumberChange = (e) => {
    const formatted = formatCardNumber(e.target.value);
    setCardNumber(formatted.replace(/\s/g, '')); // Store without spaces
  };

  const getBrandIcon = (brand) => {
    // Return empty string - no icons needed
    return '';
  };

  if (loading) {
    return (
      <PageContainer>
        <LoadingSpinner>Loading payment methods...</LoadingSpinner>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <PageHeader>
        <div>
          <PageTitle>Payment Methods</PageTitle>
          <PageSubtitle>Manage your saved payment methods</PageSubtitle>
        </div>
        <AddCardButton onClick={() => setShowAddModal(true)}>
          + Add New Card
        </AddCardButton>
      </PageHeader>

      {error && !showAddModal && <ErrorMessage>{error}</ErrorMessage>}
      {success && !showAddModal && <SuccessMessage>{success}</SuccessMessage>}

      <ContentCard>
        <CardHeader>
          <CardTitle>Saved Cards</CardTitle>
        </CardHeader>
        <CardContent>
          {paymentMethods.length === 0 ? (
            <EmptyState>
              <p>No payment methods saved yet</p>
              <p style={{ fontSize: '0.875rem', color: '#718096', marginTop: '0.5rem' }}>
                Add a card to make payments easier
              </p>
            </EmptyState>
          ) : (
            paymentMethods.map((method) => (
              <PaymentMethodCard key={method.id}>
                <PaymentMethodInfo>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <span style={{ fontSize: '2rem' }}>
                      {getBrandIcon(method.brand)}
                    </span>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <PaymentMethodBrand>{method.brand}</PaymentMethodBrand>
                        {method.isDefault && <DefaultBadge>Default</DefaultBadge>}
                      </div>
                      <PaymentMethodNumber>•••• •••• •••• {method.last4}</PaymentMethodNumber>
                      <PaymentMethodExpiry>
                        Expires {String(method.expMonth).padStart(2, '0')}/{method.expYear}
                      </PaymentMethodExpiry>
                    </div>
                  </div>
                </PaymentMethodInfo>
                <StatusBadge status={method.status}>
                  {method.status}
                </StatusBadge>
              </PaymentMethodCard>
            ))
          )}
        </CardContent>
      </ContentCard>

      {/* Add Card Modal */}
      {showAddModal && (
        <Modal>
          <ModalOverlay onClick={() => !submitting && setShowAddModal(false)} />
          <ModalContent>
            <ModalHeader>
              <ModalTitle>Add New Payment Method</ModalTitle>
            </ModalHeader>
            
            <form onSubmit={handleAddCard}>
              <ModalBody>
                {error && <ErrorMessage>{error}</ErrorMessage>}
                {success && <SuccessMessage>{success}</SuccessMessage>}

                <p style={{ marginBottom: '1.5rem', color: '#718096', fontSize: '0.875rem' }}>
                  For testing, use Stripe test card: 4242 4242 4242 4242
                </p>

                <FormGroup>
                  <Label htmlFor="cardNumber">Card Number</Label>
                  <Input
                    id="cardNumber"
                    type="text"
                    placeholder="4242 4242 4242 4242"
                    value={formatCardNumber(cardNumber)}
                    onChange={handleCardNumberChange}
                    required
                    disabled={submitting}
                    maxLength="19"
                  />
                </FormGroup>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                  <FormGroup>
                    <Label htmlFor="expMonth">Month</Label>
                    <Input
                      id="expMonth"
                      type="number"
                      placeholder="12"
                      value={expMonth}
                      onChange={(e) => setExpMonth(e.target.value)}
                      min="1"
                      max="12"
                      required
                      disabled={submitting}
                    />
                  </FormGroup>

                  <FormGroup>
                    <Label htmlFor="expYear">Year</Label>
                    <Input
                      id="expYear"
                      type="number"
                      placeholder="2025"
                      value={expYear}
                      onChange={(e) => setExpYear(e.target.value)}
                      min={new Date().getFullYear()}
                      max={new Date().getFullYear() + 20}
                      required
                      disabled={submitting}
                    />
                  </FormGroup>

                  <FormGroup>
                    <Label htmlFor="cvc">CVC</Label>
                    <Input
                      id="cvc"
                      type="text"
                      placeholder="123"
                      value={cvc}
                      onChange={(e) => setCvc(e.target.value.replace(/\D/g, '').substring(0, 4))}
                      maxLength="4"
                      required
                      disabled={submitting}
                    />
                  </FormGroup>
                </div>

                <FormGroup>
                  <CheckboxLabel>
                    <Checkbox
                      type="checkbox"
                      checked={isDefault}
                      onChange={(e) => setIsDefault(e.target.checked)}
                      disabled={submitting}
                    />
                    Set as default payment method
                  </CheckboxLabel>
                </FormGroup>
              </ModalBody>

              <ModalFooter>
                <ButtonSecondary
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  disabled={submitting}
                >
                  Cancel
                </ButtonSecondary>
                <Button type="submit" disabled={submitting}>
                  {submitting ? 'Adding...' : 'Add Card'}
                </Button>
              </ModalFooter>
            </form>
          </ModalContent>
        </Modal>
      )}
    </PageContainer>
  );
};

export default PaymentMethods;
