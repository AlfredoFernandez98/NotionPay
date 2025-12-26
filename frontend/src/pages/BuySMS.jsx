import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Elements } from '@stripe/react-stripe-js';
import { loadStripe } from '@stripe/stripe-js';
import { useAuth } from '../hooks/useAuth';
import apiFacade from '../util/apiFacade';
import { ROUTES } from '../utils/routes';
import StripePaymentForm from '../components/StripePaymentForm';
import {
  PageContainer,
  PageHeader,
  PageTitle,
  PageSubtitle,
  ContentGrid,
  ProductCard,
  ProductHeader,
  ProductName,
  ProductPrice,
  ProductDescription,
  ProductFeatures,
  FeatureItem,
  SelectButton,
  SelectedBadge,
  PaymentSection,
  SectionTitle,
  PaymentMethodSelector,
  PaymentMethodOption,
  PaymentMethodDetails,
  PaymentMethodBrand,
  PaymentMethodNumber,
  RadioButton,
  PurchaseButton,
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
  ToggleContainer,
  ToggleButton,
  ButtonSpinner,
  ConfirmationModal,
  ModalOverlay,
  ModalTitle,
  ModalText,
  ModalActions,
  ModalButton,
  InfoBox,
} from './BuySMS.styles';

// Initialize Stripe
const stripePublishableKey = import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY;
const stripePromise = loadStripe(stripePublishableKey);

const BuySMS = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  
  const [loading, setLoading] = useState(true);
  const [products, setProducts] = useState([]);
  const [paymentMethods, setPaymentMethods] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  // Payment mode
  const [useOneTimePayment, setUseOneTimePayment] = useState(false);
  const [stripeCardReady, setStripeCardReady] = useState(false);
  const [stripeElements, setStripeElements] = useState(null);
  
  // Confirmation
  const [showConfirmation, setShowConfirmation] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(ROUTES.login);
      return;
    }

    fetchData();
  }, [isAuthenticated, navigate]);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError('');
      const customerId = apiFacade.getCustomerId();

      // Fetch SMS products
      const productsData = await apiFacade.getAllProducts();
      setProducts(productsData || []);

      // Fetch payment methods
      if (customerId) {
        const methods = await apiFacade.getCustomerPaymentMethods(customerId);
        setPaymentMethods(methods || []);
        
        // Auto-select default payment method
        const defaultMethod = methods?.find(m => m.isDefault);
        if (defaultMethod && !useOneTimePayment) {
          setSelectedPaymentMethod(defaultMethod.id);
        }
      }
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load products and payment methods'));
    } finally {
      setLoading(false);
    }
  };

  const handleCardReady = (isComplete, elements) => {
    setStripeCardReady(isComplete);
    setStripeElements(elements);
  };

  const handleCardError = (errorMessage) => {
    if (errorMessage) {
      setError(errorMessage);
    } else {
      setError('');
    }
  };

  const getErrorMessage = (err, fallback) => {
    // Handle Stripe errors
    if (err.type === 'card_error' || err.type === 'validation_error') {
      return err.message;
    }
    
    // Handle API errors
    if (err.fullError) {
      return err.fullError.msg || err.fullError.message || fallback;
    }
    
    if (err.message) {
      return err.message;
    }
    
    return fallback;
  };

  const handlePurchaseClick = () => {
    // Validate selection based on payment mode
    if (!selectedProduct) {
      setError('Please select an SMS package first');
      return;
    }
    
    if (!useOneTimePayment && !selectedPaymentMethod) {
      setError('Please select a payment method or use one-time payment');
      return;
    }
    
    if (useOneTimePayment && !stripeCardReady) {
      setError('Please complete your card information');
      return;
    }

    // Show confirmation modal
    setShowConfirmation(true);
  };

  const handleConfirmPurchase = async () => {
    setShowConfirmation(false);
    setError('');
    setSuccess('');
    setProcessing(true);

    try {
      const customerId = apiFacade.getCustomerId();
      
      if (!customerId) {
        throw new Error('Session expired. Please log in again.');
      }

      const product = products.find(p => p.id === selectedProduct);
      
      if (!product) {
        throw new Error('Selected product not found');
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
          amount: product.priceCents,
          currency: product.currency,
          description: `Purchase: ${product.name}`,
          productId: product.id
        };

        response = await apiFacade.processPayment(paymentData);
        
      } else {
        // Payment with saved card
        const paymentData = {
          customerId: parseInt(customerId),
          paymentMethodId: selectedPaymentMethod,
          amount: product.priceCents,
          currency: product.currency,
          description: `Purchase: ${product.name}`,
          productId: product.id
        };

        response = await apiFacade.processPayment(paymentData);
      }
      
      setSuccess(`🎉 Successfully purchased ${product.name}! Your SMS balance has been updated.`);
      
      // Reset selections after successful purchase
      setTimeout(() => {
        setSelectedProduct(null);
        setSelectedPaymentMethod(null);
        setSuccess('');
        navigate(ROUTES.dashboard);
      }, 3000);

    } catch (err) {
      setError(getErrorMessage(err, 'Payment failed. Please try again or contact support.'));
    } finally {
      setProcessing(false);
    }
  };

  const handleTogglePaymentMode = (useStripe) => {
    setUseOneTimePayment(useStripe);
    setError('');
    
    // Auto-select default method when switching to saved cards
    if (!useStripe && !selectedPaymentMethod) {
      const defaultMethod = paymentMethods.find(m => m.isDefault);
      if (defaultMethod) {
        setSelectedPaymentMethod(defaultMethod.id);
      }
    }
  };

  const formatPrice = (cents) => {
    return (cents / 100).toFixed(2);
  };

  const getBrandIcon = (brand) => {
    const icons = {
      'visa': '💳',
      'mastercard': '💳',
      'amex': '💳',
      'discover': '💳',
    };
    return icons[brand?.toLowerCase()] || '💳';
  };

  if (loading) {
    return (
      <PageContainer>
        <LoadingSpinner role="status" aria-live="polite">
          <span>Loading products...</span>
        </LoadingSpinner>
      </PageContainer>
    );
  }

  const selectedProductData = products.find(p => p.id === selectedProduct);
  const selectedPaymentMethodData = paymentMethods.find(pm => pm.id === selectedPaymentMethod);
  
  const isPurchaseDisabled = !selectedProduct || 
    processing || 
    (!useOneTimePayment && !selectedPaymentMethod) ||
    (useOneTimePayment && !stripeCardReady);

  return (
    <PageContainer>
      <PageHeader>
        <PageTitle>Buy SMS Credits</PageTitle>
        <PageSubtitle>Choose a package and complete your purchase securely</PageSubtitle>
      </PageHeader>

      {error && (
        <ErrorMessage role="alert" aria-live="assertive">
          ⚠️ {error}
        </ErrorMessage>
      )}
      {success && (
        <SuccessMessage role="status" aria-live="polite">
          {success}
        </SuccessMessage>
      )}

      {products.length === 0 ? (
        <EmptyState role="status">
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }} aria-hidden="true">📦</div>
          <p>No SMS packages available at the moment</p>
        </EmptyState>
      ) : (
        <>
          <SectionTitle id="select-package">1. Select SMS Package</SectionTitle>
          <ContentGrid role="group" aria-labelledby="select-package">
            {products.map((product) => (
              <ProductCard
                key={product.id}
                selected={selectedProduct === product.id}
                onClick={() => setSelectedProduct(product.id)}
                role="button"
                tabIndex={0}
                aria-pressed={selectedProduct === product.id}
                onKeyPress={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault();
                    setSelectedProduct(product.id);
                  }
                }}
              >
                {selectedProduct === product.id && (
                  <SelectedBadge aria-label="Selected">✓ Selected</SelectedBadge>
                )}
                <ProductHeader>
                  <ProductName>{product.name}</ProductName>
                  <ProductPrice>
                    {formatPrice(product.priceCents)} {product.currency}
                  </ProductPrice>
                </ProductHeader>
                <ProductDescription>{product.description}</ProductDescription>
                <ProductFeatures>
                  <FeatureItem>💬 {product.smsCount} SMS Credits</FeatureItem>
                  <FeatureItem>✓ Never expires</FeatureItem>
                  <FeatureItem>
                    📊 {(product.priceCents / product.smsCount / 100).toFixed(3)} {product.currency}/SMS
                  </FeatureItem>
                </ProductFeatures>
                <SelectButton 
                  selected={selectedProduct === product.id}
                  aria-hidden="true"
                  tabIndex={-1}
                >
                  {selectedProduct === product.id ? 'Selected' : 'Select Package'}
                </SelectButton>
              </ProductCard>
            ))}
          </ContentGrid>

          <PaymentSection>
            <SectionTitle id="select-payment">2. Select Payment Method</SectionTitle>
            
            {/* Payment Method Toggle */}
            <ToggleContainer role="tablist" aria-label="Payment method type">
              <ToggleButton
                role="tab"
                aria-selected={!useOneTimePayment}
                aria-controls="saved-cards-panel"
                $active={!useOneTimePayment}
                onClick={() => handleTogglePaymentMode(false)}
              >
                Use Saved Card
              </ToggleButton>
              <ToggleButton
                role="tab"
                aria-selected={useOneTimePayment}
                aria-controls="one-time-payment-panel"
                $active={useOneTimePayment}
                onClick={() => handleTogglePaymentMode(true)}
              >
                Pay Without Saving
              </ToggleButton>
            </ToggleContainer>

            {useOneTimePayment ? (
              /* One-time Payment with Stripe Elements */
              <div 
                id="one-time-payment-panel" 
                role="tabpanel" 
                aria-labelledby="select-payment"
              >
                <Elements stripe={stripePromise}>
                  <StripePaymentForm
                    onCardReady={handleCardReady}
                    onError={handleCardError}
                  />
                </Elements>
              </div>
            ) : paymentMethods.length === 0 ? (
              <EmptyState role="status">
                <div style={{ fontSize: '3rem', marginBottom: '1rem' }} aria-hidden="true">💳</div>
                <p>No payment methods saved</p>
                <AddCardLink 
                  onClick={() => navigate(ROUTES.paymentMethods)}
                  aria-label="Add a new payment method"
                >
                  + Add a payment method
                </AddCardLink>
              </EmptyState>
            ) : (
              <PaymentMethodSelector
                id="saved-cards-panel"
                role="tabpanel"
                aria-labelledby="select-payment"
              >
                {paymentMethods.map((method) => (
                  <PaymentMethodOption
                    key={method.id}
                    selected={selectedPaymentMethod === method.id}
                    onClick={() => setSelectedPaymentMethod(method.id)}
                    role="radio"
                    aria-checked={selectedPaymentMethod === method.id}
                    tabIndex={0}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        setSelectedPaymentMethod(method.id);
                      }
                    }}
                  >
                    <RadioButton
                      type="radio"
                      checked={selectedPaymentMethod === method.id}
                      onChange={() => setSelectedPaymentMethod(method.id)}
                      aria-hidden="true"
                      tabIndex={-1}
                    />
                    <span style={{ fontSize: '1.5rem', marginRight: '12px' }} aria-hidden="true">
                      {getBrandIcon(method.brand)}
                    </span>
                    <PaymentMethodDetails>
                      <PaymentMethodBrand>
                        {method.brand}
                        {method.isDefault && (
                          <span style={{
                            marginLeft: '8px',
                            padding: '2px 8px',
                            background: '#6BB8E8',
                            color: 'white',
                            borderRadius: '12px',
                            fontSize: '0.75rem',
                            fontWeight: '600'
                          }}>
                            Default
                          </span>
                        )}
                      </PaymentMethodBrand>
                      <PaymentMethodNumber>
                        •••• •••• •••• {method.last4}
                      </PaymentMethodNumber>
                      <div style={{ fontSize: '0.875rem', color: '#718096' }}>
                        Expires {String(method.expMonth).padStart(2, '0')}/{method.expYear}
                      </div>
                    </PaymentMethodDetails>
                  </PaymentMethodOption>
                ))}
              </PaymentMethodSelector>
            )}
          </PaymentSection>

          {selectedProductData && (
            <Summary>
              <SectionTitle>Purchase Summary</SectionTitle>
              <SummaryRow>
                <SummaryLabel>Package:</SummaryLabel>
                <SummaryValue>{selectedProductData.name}</SummaryValue>
              </SummaryRow>
              <SummaryRow>
                <SummaryLabel>SMS Credits:</SummaryLabel>
                <SummaryValue>{selectedProductData.smsCount}</SummaryValue>
              </SummaryRow>
              <SummaryRow>
                <SummaryLabel>Subtotal:</SummaryLabel>
                <SummaryValue>
                  {formatPrice(selectedProductData.priceCents)} {selectedProductData.currency}
                </SummaryValue>
              </SummaryRow>
              <TotalRow>
                <SummaryLabel style={{ fontSize: '1.25rem', fontWeight: '700' }}>
                  Total:
                </SummaryLabel>
                <SummaryValue style={{ fontSize: '1.5rem', fontWeight: '700', color: '#6BB8E8' }}>
                  {formatPrice(selectedProductData.priceCents)} {selectedProductData.currency}
                </SummaryValue>
              </TotalRow>

              <PurchaseButton
                onClick={handlePurchaseClick}
                disabled={isPurchaseDisabled}
                aria-label={`Complete purchase of ${selectedProductData.name} for ${formatPrice(selectedProductData.priceCents)} ${selectedProductData.currency}`}
              >
                {processing ? (
                  <>
                    <ButtonSpinner aria-hidden="true" />
                    Processing...
                  </>
                ) : (
                  'Complete Purchase'
                )}
              </PurchaseButton>
            </Summary>
          )}
        </>
      )}

      {/* Confirmation Modal */}
      {showConfirmation && selectedProductData && (
        <>
          <ModalOverlay 
            onClick={() => setShowConfirmation(false)}
            aria-hidden="true"
          />
          <ConfirmationModal role="dialog" aria-labelledby="confirm-title" aria-modal="true">
            <ModalTitle id="confirm-title">Confirm Purchase</ModalTitle>
            <ModalText>
              You are about to purchase <strong>{selectedProductData.name}</strong> for{' '}
              <strong>{formatPrice(selectedProductData.priceCents)} {selectedProductData.currency}</strong>.
              {!useOneTimePayment && selectedPaymentMethodData && (
                <>
                  <br /><br />
                  Payment method: {selectedPaymentMethodData.brand} ending in {selectedPaymentMethodData.last4}
                </>
              )}
            </ModalText>
            <ModalActions>
              <ModalButton 
                onClick={() => setShowConfirmation(false)}
                aria-label="Cancel purchase"
              >
                Cancel
              </ModalButton>
              <ModalButton 
                $variant="primary" 
                onClick={handleConfirmPurchase}
                aria-label="Confirm and complete purchase"
              >
                Confirm Purchase
              </ModalButton>
            </ModalActions>
          </ConfirmationModal>
        </>
      )}
    </PageContainer>
  );
};

export default BuySMS;
