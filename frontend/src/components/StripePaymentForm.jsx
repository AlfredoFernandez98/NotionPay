import { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import {
  CardFormContainer,
  FormGroup,
  FormLabel,
  InfoBox,
  StripeElementContainer,
} from '../pages/BuySMS.styles';

const CARD_ELEMENT_OPTIONS = {
  style: {
    base: {
      fontSize: '16px',
      color: '#1A202C',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      '::placeholder': {
        color: '#A0AEC0',
      },
      iconColor: '#6BB8E8',
    },
    invalid: {
      color: '#E53E3E',
      iconColor: '#E53E3E',
    },
  },
  hidePostalCode: true,
};

const StripePaymentForm = ({ onCardReady, onError }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [cardFocused, setCardFocused] = useState(false);
  const [cardError, setCardError] = useState(null);
  const [cardComplete, setCardComplete] = useState(false);

  const handleCardChange = (event) => {
    setCardError(event.error ? event.error.message : null);
    setCardComplete(event.complete);
    
    if (onError) {
      onError(event.error ? event.error.message : null);
    }
    
    if (onCardReady) {
      onCardReady(event.complete, elements);
    }
  };

  return (
    <CardFormContainer>
      <FormGroup>
        <FormLabel htmlFor="card-element" $required>
          Card Information
        </FormLabel>
        <StripeElementContainer
          $focused={cardFocused}
          $error={!!cardError}
          role="group"
          aria-label="Credit card input"
        >
          <CardElement
            id="card-element"
            options={CARD_ELEMENT_OPTIONS}
            onFocus={() => setCardFocused(true)}
            onBlur={() => setCardFocused(false)}
            onChange={handleCardChange}
          />
        </StripeElementContainer>
        {cardError && (
          <InfoBox $variant="warning" role="alert">
            {cardError}
          </InfoBox>
        )}
      </FormGroup>

      <InfoBox $variant="info">
        <div>
          <strong>Secure Payment</strong>
          <br />
          Your card information is encrypted and never stored on our servers. 
          Powered by Stripe for maximum security.
          <br />
          <strong>Test Card:</strong> 4242 4242 4242 4242 (any future date, any CVC)
        </div>
      </InfoBox>
    </CardFormContainer>
  );
};

export default StripePaymentForm;


