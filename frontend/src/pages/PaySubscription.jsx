import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { useAuth } from '../hooks/useAuth';
import apiFacade from '../util/apiFacade';
import { ROUTES } from '../utils/routes';
import StripePaymentForm from '../components/StripePaymentForm';
import { formatDate } from '../utils/dateFormatter';
import {
  PageContainer,
  PageHeader,
  PageTitle,
  PageSubtitle,
  PaymentMethodSelector,
  PaymentMethodOption,
  PaymentMethodDetails,
  PaymentMethodBrand,
  PaymentMethodNumber,
  RadioButton,
  ToggleContainer,
  ToggleButton,
  PurchaseButton,
  ButtonSpinner,
  Summary,
  SummaryRow,
  SummaryLabel,
  SummaryValue,
  TotalRow,
  LoadingSpinner,
  ErrorMessage,
  SuccessMessage,
  EmptyState,
  AddCardLink,
  ConfirmationModal,
  ModalOverlay,
  ModalTitle,
  ModalText,
  ModalActions,
  ModalButton,
} from './BuySMS.styles';
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  InfoRow,
  InfoLabel,
  InfoValue,
  StatusBadge,
} from './Dashboard.styles';

// Initialize Stripe
const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;
const stripePromise = loadStripe(stripePublishableKey);

const PaySubscription = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [subscription, setSubscription] = useState(null);
  const [plan, setPlan] = useState(null);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Payment mode
  const [useOneTimePayment, setUseOneTimePayment] = useState(false);
  const [stripeCardReady, setStripeCardReady] = useState(false);
  const [stripeElements, setStripeElements] = useState(null);
  
  // Confirmation and Success Modals
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [paymentDetails, setPaymentDetails] = useState(null);

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const customerId = apiFacade.getCustomerId();

      if (!customerId) {
        throw new Error('Customer ID not found');
      }

      // Fetch subscription
      const subData = await apiFacade.getCustomerSubscription(customerId);
      setSubscription(subData);

      // Fetch plan details
      if (subData?.planId) {
        const planData = await apiFacade.getPlanById(subData.planId);
        setPlan(planData);
      }

      // Fetch payment methods
      const methods = await apiFacade.getCustomerPaymentMethods(customerId);
      setPaymentMethods(methods || []);
      
      // Auto-select default payment method
      const defaultMethod = methods?.find(m => m.isDefault);
      if (defaultMethod && !useOneTimePayment) {
        setSelectedPaymentMethod(defaultMethod.id);
      }

    } catch (err) {
      console.error('Error fetching subscription data:', err);
      setError('Failed to load subscription details');
    } finally {
      setLoading(false);
    }
  }, [useOneTimePayment]);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    fetchData();
  }, [isAuthenticated, navigate, fetchData]);

  const handleCardReady = (isComplete, elements) => {
    setStripeCardReady(isComplete);
    setStripeElements(elements);
  };

  const handleCardError = (errorMessage) => {
    setError(errorMessage);
  };

  const handleTogglePaymentMode = (useOnetime) => {
    setUseOneTimePayment(useOnetime);
    setError('');
    if (!useOnetime && paymentMethods.length > 0) {
      const defaultMethod = paymentMethods.find(m => m.isDefault);
      setSelectedPaymentMethod(defaultMethod?.id || paymentMethods[0]?.id);
    }
  };

  const handlePaySubscription = () => {
    // Validate before showing confirmation
    if (!useOneTimePayment && !selectedPaymentMethod) {
      setError('Please select a payment method');
      return;
    }

    if (useOneTimePayment && !stripeCardReady) {
      setError('Please complete your card details');
      return;
    }

    // Show confirmation modal
    setShowConfirmation(true);
  };

  const handleConfirmPayment = async () => {
    setShowConfirmation(false);
    setError('');
    setSuccess('');
    setProcessing(true);

    try {
      const customerId = apiFacade.getCustomerId();
      
      if (!customerId || !subscription || !plan) {
        throw new Error('Missing subscription data');
      }

      let response;
      
      if (useOneTimePayment) {
        // One-time payment with Stripe Elements
        const stripe = await stripePromise;
        const cardElement = stripeElements.getElement('card');
        
        // Create payment method with Stripe
        const { error: stripeError, paymentMethod } = await stripe.createPaymentMethod({
          type: 'card',
          card: cardElement,
        });

        if (stripeError) {
          throw stripeError;
        }

        // Process payment with backend using Stripe payment method ID
        const paymentData = {
          customerId: parseInt(customerId),
          paymentMethodId: paymentMethod.id,
          amount: plan.priceCents,
          currency: plan.currency,
          description: `Subscription Payment: ${plan.name}`,
          subscriptionId: subscription.id
        };

        response = await apiFacade.processPayment(paymentData);
        
      } else {
        // Payment with saved card
        const paymentData = {
          customerId: parseInt(customerId),
          paymentMethodId: selectedPaymentMethod,
          amount: plan.priceCents,
          currency: plan.currency,
          description: `Subscription Payment: ${plan.name}`,
          subscriptionId: subscription.id
        };

        response = await apiFacade.processPayment(paymentData);
      }
      
      // Store payment details for success modal
      setPaymentDetails({
        planName: plan.name,
        amount: plan.priceCents / 100,
        currency: plan.currency.toUpperCase(),
        nextBillingDate: formatDate(new Date(Date.now() + (plan.period === 'MONTHLY' ? 30 : 365) * 24 * 60 * 60 * 1000)),
        paymentId: response.paymentId,
        timestamp: new Date().toLocaleString('da-DK')
      });
      
      setSuccess(`Subscription payment successful! Your subscription has been renewed.`);
      setShowSuccessModal(true);
      
    } catch (err) {
      console.error('Payment error:', err);
      setError(err.message || 'Payment failed. Please try again.');
    } finally {
      setProcessing(false);
    }
  };

  const getBrandIcon = (brand) => {
    const icons = {
      'visa': 'Card',
      'mastercard': 'Card',
      'amex': 'Card',
      'discover': 'Card'
    };
    return icons[brand?.toLowerCase()] || 'Card';
  };

  const isPaymentDue = () => {
    if (!subscription?.nextBillingDate) return false;
    const today = new Date();
    const billingDate = new Date(subscription.nextBillingDate);
    return billingDate <= today;
  };

  if (loading) {
    return (
      <PageContainer>
        <LoadingSpinner>Loading subscription details...</LoadingSpinner>
      </PageContainer>
    );
  }

  if (!subscription || !plan) {
    return (
      <PageContainer>
        <ErrorMessage>No active subscription found</ErrorMessage>
      </PageContainer>
    );
  }

  return (
    <PageContainer>
      <PageHeader>
        <PageTitle>Pay Subscription</PageTitle>
        <PageSubtitle>Complete your subscription payment</PageSubtitle>
      </PageHeader>

      {error && <ErrorMessage>{error}</ErrorMessage>}
      {success && <SuccessMessage>{success}</SuccessMessage>}

      <Card>
        <CardHeader>
          <CardTitle>Subscription Details</CardTitle>
        </CardHeader>
        <CardContent>
          <InfoRow>
            <InfoLabel>Plan</InfoLabel>
            <InfoValue>{plan.name}</InfoValue>
          </InfoRow>
          <InfoRow>
            <InfoLabel>Status</InfoLabel>
            <StatusBadge status={subscription.status}>
              {subscription.status}
            </StatusBadge>
          </InfoRow>
          <InfoRow>
            <InfoLabel>Billing Period</InfoLabel>
            <InfoValue>{plan.period}</InfoValue>
          </InfoRow>
          <InfoRow>
            <InfoLabel>Next Billing Date</InfoLabel>
            <InfoValue>
              {formatDate(subscription.nextBillingDate)}
              {isPaymentDue() && (
                <span style={{ color: '#e53e3e', fontWeight: 'bold', marginLeft: '0.5rem' }}>
                  (Payment Due)
                </span>
              )}
            </InfoValue>
          </InfoRow>
          <InfoRow>
            <InfoLabel>Amount</InfoLabel>
            <InfoValue style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#2d3748' }}>
              {(plan.priceCents / 100).toFixed(2)} {plan.currency}
            </InfoValue>
          </InfoRow>
        </CardContent>
      </Card>

      <Card style={{ marginTop: '1.5rem' }}>
        <CardHeader>
          <CardTitle>Select Payment Method</CardTitle>
        </CardHeader>
        <CardContent>
          {/* Payment Method Toggle */}
          <ToggleContainer role="tablist" aria-label="Payment method type">
            <ToggleButton
              role="tab"
              aria-selected={!useOneTimePayment}
              $active={!useOneTimePayment}
              onClick={() => handleTogglePaymentMode(false)}
            >
              Use Saved Card
            </ToggleButton>
            <ToggleButton
              role="tab"
              aria-selected={useOneTimePayment}
              $active={useOneTimePayment}
              onClick={() => handleTogglePaymentMode(true)}
            >
              Pay Without Saving
            </ToggleButton>
          </ToggleContainer>

          {useOneTimePayment ? (
            <div style={{ marginTop: '1rem' }}>
              <Elements stripe={stripePromise}>
                <StripePaymentForm
                  onCardReady={handleCardReady}
                  onError={handleCardError}
                />
              </Elements>
            </div>
          ) : paymentMethods.length === 0 ? (
            <EmptyState>
              <p>No payment methods saved</p>
              <AddCardLink onClick={() => navigate(ROUTES.paymentMethods)}>
                + Add a payment method
              </AddCardLink>
            </EmptyState>
          ) : (
            <PaymentMethodSelector>
              {paymentMethods.map((method) => (
                <PaymentMethodOption
                  key={method.id}
                  $selected={selectedPaymentMethod === method.id}
                  onClick={() => setSelectedPaymentMethod(method.id)}
                >
                  <RadioButton
                    type="radio"
                    checked={selectedPaymentMethod === method.id}
                    onChange={() => setSelectedPaymentMethod(method.id)}
                  />
                  <PaymentMethodDetails>
                    <PaymentMethodBrand>
                      {getBrandIcon(method.brand)} {method.brand?.toUpperCase()}
                      {method.isDefault && (
                        <span style={{ 
                          marginLeft: '0.5rem', 
                          fontSize: '0.75rem', 
                          color: '#48bb78',
                          fontWeight: 'bold'
                        }}>
                          DEFAULT
                        </span>
                      )}
                    </PaymentMethodBrand>
                    <PaymentMethodNumber>
                      •••• •••• •••• {method.last4}
                    </PaymentMethodNumber>
                    <PaymentMethodNumber>
                      Expires: {method.expMonth}/{method.expYear}
                    </PaymentMethodNumber>
                  </PaymentMethodDetails>
                </PaymentMethodOption>
              ))}
            </PaymentMethodSelector>
          )}
        </CardContent>
      </Card>

      <Summary style={{ marginTop: '1.5rem' }}>
        <SummaryRow>
          <SummaryLabel>Subscription</SummaryLabel>
          <SummaryValue>{plan.name}</SummaryValue>
        </SummaryRow>
        <SummaryRow>
          <SummaryLabel>Period</SummaryLabel>
          <SummaryValue>{plan.period}</SummaryValue>
        </SummaryRow>
        <TotalRow>
          <SummaryLabel style={{ fontSize: '1.25rem' }}>Total</SummaryLabel>
          <SummaryValue style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
            {(plan.priceCents / 100).toFixed(2)} {plan.currency}
          </SummaryValue>
        </TotalRow>
      </Summary>

      <PurchaseButton
        onClick={handlePaySubscription}
        disabled={processing || (!useOneTimePayment && !selectedPaymentMethod) || (useOneTimePayment && !stripeCardReady)}
      >
        {processing ? (
          <>
            <ButtonSpinner /> Processing Payment...
          </>
        ) : (
          `Pay ${(plan.priceCents / 100).toFixed(2)} ${plan.currency}`
        )}
      </PurchaseButton>

      {/* Confirmation Modal */}
      {showConfirmation && plan && (
        <>
          <ModalOverlay 
            onClick={() => setShowConfirmation(false)}
            aria-hidden="true"
          />
          <ConfirmationModal role="dialog" aria-labelledby="confirm-title" aria-modal="true">
            <ModalTitle id="confirm-title">Confirm Subscription Payment</ModalTitle>
            <ModalText>
              You are about to pay for your <strong>{plan.name}</strong> subscription for{' '}
              <strong>{(plan.priceCents / 100).toFixed(2)} {plan.currency}</strong>.
              {!useOneTimePayment && selectedPaymentMethod && (
                <>
                  <br /><br />
                  Payment method: {paymentMethods.find(m => m.id === selectedPaymentMethod)?.brand} ending in {paymentMethods.find(m => m.id === selectedPaymentMethod)?.last4}
                </>
              )}
              <br /><br />
              This will renew your subscription and update your next billing date.
            </ModalText>
            <ModalActions>
              <ModalButton 
                onClick={() => setShowConfirmation(false)}
                aria-label="Cancel payment"
              >
                Cancel
              </ModalButton>
              <ModalButton 
                $variant="primary" 
                onClick={handleConfirmPayment}
                aria-label="Confirm and complete payment"
              >
                Confirm Payment
              </ModalButton>
            </ModalActions>
          </ConfirmationModal>
        </>
      )}

      {/* Success Modal */}
      {showSuccessModal && paymentDetails && (
        <>
          <ModalOverlay onClick={() => {}} aria-hidden="true" />
          <ConfirmationModal role="dialog" aria-labelledby="success-title" aria-modal="true">
            <div style={{ textAlign: 'center', marginBottom: '20px' }}>
              <div style={{ 
                fontSize: '4rem', 
                color: '#48BB78',
                marginBottom: '10px'
              }}>
                ✓
              </div>
              <ModalTitle id="success-title" style={{ color: '#48BB78' }}>
                Payment Successful!
              </ModalTitle>
            </div>
            
            <div style={{ 
              background: '#F7FAFC', 
              padding: '20px', 
              borderRadius: '8px',
              marginBottom: '20px',
              textAlign: 'left'
            }}>
              <div style={{ marginBottom: '12px' }}>
                <strong>Subscription:</strong> {paymentDetails.planName}
              </div>
              <div style={{ marginBottom: '12px' }}>
                <strong>Amount Paid:</strong> {paymentDetails.amount} {paymentDetails.currency}
              </div>
              <div style={{ marginBottom: '12px' }}>
                <strong>Next Billing Date:</strong> {paymentDetails.nextBillingDate}
              </div>
              <div style={{ marginBottom: '12px' }}>
                <strong>Payment ID:</strong> #{paymentDetails.paymentId}
              </div>
              <div>
                <strong>Date:</strong> {paymentDetails.timestamp}
              </div>
            </div>
            
            <ModalText style={{ marginBottom: '20px', color: '#48BB78' }}>
              Your subscription has been renewed successfully!
            </ModalText>
            
            <ModalActions>
              <ModalButton 
                $variant="primary" 
                onClick={() => navigate(ROUTES.dashboard)}
                aria-label="Return to dashboard"
                style={{ width: '100%' }}
              >
                Return to Dashboard
              </ModalButton>
            </ModalActions>
          </ConfirmationModal>
        </>
      )}
    </PageContainer>
  );
};

export default PaySubscription;

