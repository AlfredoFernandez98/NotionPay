import styled, { keyframes } from 'styled-components';

// Animations
const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
`;

const spin = keyframes`
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
`;

export const PageContainer = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
`;

export const PageHeader = styled.div`
  text-align: center;
  margin-bottom: 3rem;
`;

export const PageTitle = styled.h1`
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 0.5rem 0;
`;

export const PageSubtitle = styled.p`
  font-size: 1.125rem;
  color: #718096;
  margin: 0;
`;

export const SectionTitle = styled.h2`
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin: 2rem 0 1.5rem 0;
`;

export const ContentGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
`;

export const ProductCard = styled.div`
  position: relative;
  background: white;
  border: 3px solid ${props => props.selected ? '#6BB8E8' : '#e2e8f0'};
  border-radius: 12px;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: ${props => props.selected ? '0 8px 16px rgba(107, 184, 232, 0.3)' : '0 2px 4px rgba(0, 0, 0, 0.1)'};

  &:hover {
    border-color: #6BB8E8;
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(107, 184, 232, 0.2);
  }
`;

export const SelectedBadge = styled.div`
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: #6BB8E8;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
`;

export const ProductHeader = styled.div`
  margin-bottom: 1rem;
`;

export const ProductName = styled.h3`
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 0.5rem 0;
`;

export const ProductPrice = styled.div`
  font-size: 2rem;
  font-weight: 700;
  color: #6BB8E8;
  margin: 0;
`;

export const ProductDescription = styled.p`
  font-size: 1rem;
  color: #4a5568;
  margin: 1rem 0;
  line-height: 1.5;
`;

export const ProductFeatures = styled.ul`
  list-style: none;
  padding: 0;
  margin: 1.5rem 0;
`;

export const FeatureItem = styled.li`
  font-size: 0.875rem;
  color: #4a5568;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e2e8f0;

  &:last-child {
    border-bottom: none;
  }
`;

export const SelectButton = styled.button`
  width: 100%;
  padding: 0.75rem;
  background: ${props => props.selected ? '#5AA7D7' : '#6BB8E8'};
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 1rem;

  &:hover {
    background: #5AA7D7;
    transform: scale(1.02);
  }
`;

export const PaymentSection = styled.div`
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
`;

export const PaymentMethodSelector = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

export const PaymentMethodOption = styled.div`
  display: flex;
  align-items: center;
  padding: 1.5rem;
  border: 2px solid ${props => props.selected ? '#6BB8E8' : '#e2e8f0'};
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: ${props => props.selected ? '#f0f9ff' : 'white'};

  &:hover {
    border-color: #6BB8E8;
  }
`;

export const PaymentMethodDetails = styled.div`
  flex: 1;
`;

export const PaymentMethodBrand = styled.div`
  font-weight: 600;
  color: #2d3748;
  text-transform: capitalize;
  margin-bottom: 0.25rem;
  display: flex;
  align-items: center;
`;

export const PaymentMethodNumber = styled.div`
  font-size: 1rem;
  color: #4a5568;
  font-family: 'Courier New', monospace;
`;

export const RadioButton = styled.input`
  width: 1.5rem;
  height: 1.5rem;
  margin-right: 1rem;
  cursor: pointer;
  accent-color: #6BB8E8;
`;

export const Summary = styled.div`
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 2rem;
  max-width: 600px;
  margin: 0 auto;
`;

export const SummaryRow = styled.div`
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e2e8f0;
`;

export const SummaryLabel = styled.span`
  font-size: 1rem;
  color: #4a5568;
  font-weight: 500;
`;

export const SummaryValue = styled.span`
  font-size: 1rem;
  color: #1a202c;
  font-weight: 600;
`;

export const TotalRow = styled(SummaryRow)`
  border-top: 2px solid #cbd5e0;
  border-bottom: none;
  margin-top: 1rem;
  padding-top: 1rem;
`;

export const PurchaseButton = styled.button`
  width: 100%;
  padding: 1rem;
  background: #6BB8E8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.125rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 2rem;

  &:hover:not(:disabled) {
    background: #5AA7D7;
    transform: translateY(-2px);
    box-shadow: 0 8px 16px rgba(107, 184, 232, 0.3);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
`;

export const LoadingSpinner = styled.div`
  text-align: center;
  padding: 4rem;
  font-size: 1.25rem;
  color: #6BB8E8;
`;

export const ErrorMessage = styled.div`
  padding: 1rem 1.5rem;
  background: #fee;
  color: #c33;
  border-radius: 8px;
  margin-bottom: 2rem;
  font-size: 1rem;
  text-align: center;
`;

export const SuccessMessage = styled.div`
  padding: 1rem 1.5rem;
  background: #d4edda;
  color: #155724;
  border-radius: 8px;
  margin-bottom: 2rem;
  font-size: 1rem;
  text-align: center;
`;

export const EmptyState = styled.div`
  text-align: center;
  padding: 4rem;
  color: #718096;

  p {
    margin: 0;
    font-size: 1.125rem;
  }
`;

export const AddCardLink = styled.button`
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  background: #6BB8E8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #5AA7D7;
    transform: translateY(-2px);
  }
`;

// Payment Method Toggle
export const ToggleContainer = styled.div`
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 8px;
  background: #F7FAFC;
  border-radius: 8px;
  border: 2px solid #E2E8F0;
  animation: ${fadeIn} 0.3s ease-out;
`;

export const ToggleButton = styled.button`
  flex: 1;
  padding: 12px;
  border-radius: 6px;
  border: none;
  background: ${props => props.$active ? '#6BB8E8' : 'transparent'};
  color: ${props => props.$active ? 'white' : '#4A5568'};
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.95rem;

  &:hover {
    background: ${props => props.$active ? '#5AA7D7' : '#EDF2F7'};
  }

  &:focus-visible {
    outline: 2px solid #6BB8E8;
    outline-offset: 2px;
  }

  &:active {
    transform: scale(0.98);
  }
`;

// Stripe Card Form
export const CardFormContainer = styled.div`
  background: white;
  padding: 24px;
  border-radius: 12px;
  border: 2px solid #E2E8F0;
  animation: ${fadeIn} 0.3s ease-out;
`;

export const FormGroup = styled.div`
  margin-bottom: ${props => props.$compact ? '12px' : '20px'};
`;

export const FormLabel = styled.label`
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2D3748;
  font-size: 0.95rem;

  ${props => props.$required && `
    &::after {
      content: ' *';
      color: #E53E3E;
    }
  `}
`;

export const InfoBox = styled.div`
  margin-top: 16px;
  padding: 12px 16px;
  background: ${props => {
    if (props.$variant === 'warning') return '#FEF3C7';
    if (props.$variant === 'info') return '#DBEAFE';
    if (props.$variant === 'success') return '#D1FAE5';
    return '#F3F4F6';
  }};
  border-radius: 8px;
  font-size: 0.875rem;
  color: ${props => {
    if (props.$variant === 'warning') return '#92400E';
    if (props.$variant === 'info') return '#1E40AF';
    if (props.$variant === 'success') return '#065F46';
    return '#374151';
  }};
  border-left: 4px solid ${props => {
    if (props.$variant === 'warning') return '#F59E0B';
    if (props.$variant === 'info') return '#3B82F6';
    if (props.$variant === 'success') return '#10B981';
    return '#6B7280';
  }};
  display: flex;
  align-items: flex-start;
  gap: 8px;
  line-height: 1.5;
  animation: ${fadeIn} 0.3s ease-out;
`;

export const ButtonSpinner = styled.span`
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: ${spin} 0.6s linear infinite;
  margin-right: 8px;
`;

export const StripeElementContainer = styled.div`
  padding: 12px;
  border-radius: 8px;
  border: 2px solid ${props => props.$focused ? '#6BB8E8' : props.$error ? '#E53E3E' : '#E2E8F0'};
  transition: all 0.2s;
  background: white;

  ${props => props.$focused && `
    box-shadow: 0 0 0 3px rgba(107, 184, 232, 0.1);
  `}

  ${props => props.$error && props.$focused && `
    box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1);
  `}
`;

export const ConfirmationModal = styled.div`
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  max-width: 400px;
  width: 90%;
  z-index: 1001;
  animation: ${fadeIn} 0.2s ease-out;
`;

export const ModalOverlay = styled.div`
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  animation: ${fadeIn} 0.2s ease-out;
`;

export const ModalTitle = styled.h3`
  margin: 0 0 16px 0;
  font-size: 1.5rem;
  color: #1A202C;
`;

export const ModalText = styled.p`
  margin: 0 0 24px 0;
  color: #4A5568;
  line-height: 1.6;
`;

export const ModalActions = styled.div`
  display: flex;
  gap: 12px;
  justify-content: flex-end;
`;

export const ModalButton = styled.button`
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  ${props => props.$variant === 'primary' ? `
    background: #6BB8E8;
    color: white;

    &:hover:not(:disabled) {
      background: #5AA7D7;
    }
  ` : `
    background: #E2E8F0;
    color: #4A5568;

    &:hover:not(:disabled) {
      background: #CBD5E0;
    }
  `}

  &:focus-visible {
    outline: 2px solid #6BB8E8;
    outline-offset: 2px;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;
