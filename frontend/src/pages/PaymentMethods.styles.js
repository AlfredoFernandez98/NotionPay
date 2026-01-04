import styled from 'styled-components';

export const PageContainer = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
`;

export const PageHeader = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
`;

export const PageTitle = styled.h1`
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
`;

export const PageSubtitle = styled.p`
  font-size: 1rem;
  color: #718096;
  margin: 0.5rem 0 0 0;
`;

export const ContentCard = styled.div`
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
`;

export const CardHeader = styled.div`
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
`;

export const CardTitle = styled.h2`
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
`;

export const CardContent = styled.div`
  padding: 1.5rem;
`;

export const PaymentMethodCard = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: all 0.2s;

  &:hover {
    border-color: #6BB8E8;
    box-shadow: 0 4px 6px rgba(107, 184, 232, 0.1);
  }

  &:last-child {
    margin-bottom: 0;
  }
`;

export const PaymentMethodInfo = styled.div`
  flex: 1;
`;

export const PaymentMethodBrand = styled.span`
  font-weight: 600;
  color: #2d3748;
  text-transform: capitalize;
`;

export const PaymentMethodNumber = styled.div`
  font-size: 1rem;
  color: #4a5568;
  margin-top: 0.25rem;
  font-family: 'Courier New', monospace;
`;

export const PaymentMethodExpiry = styled.div`
  font-size: 0.875rem;
  color: #718096;
  margin-top: 0.25rem;
`;

export const DefaultBadge = styled.span`
  display: inline-block;
  padding: 0.25rem 0.75rem;
  background: #6BB8E8;
  color: white;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
`;

export const StatusBadge = styled.span`
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  background: ${props => {
    switch (props.status) {
      case 'ACTIVE': return '#d4edda';
      case 'INACTIVE': return '#f8d7da';
      default: return '#e2e8f0';
    }
  }};
  color: ${props => {
    switch (props.status) {
      case 'ACTIVE': return '#155724';
      case 'INACTIVE': return '#721c24';
      default: return '#4a5568';
    }
  }};
`;

export const AddCardButton = styled.button`
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
    box-shadow: 0 4px 8px rgba(107, 184, 232, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
`;

export const Modal = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
`;

export const ModalOverlay = styled.div`
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
`;

export const ModalContent = styled.div`
  position: relative;
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
`;

export const ModalHeader = styled.div`
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
`;

export const ModalTitle = styled.h2`
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
`;

export const ModalBody = styled.div`
  padding: 1.5rem;
`;

export const ModalFooter = styled.div`
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
`;

export const FormGroup = styled.div`
  margin-bottom: 1.5rem;

  &:last-child {
    margin-bottom: 0;
  }
`;

export const Label = styled.label`
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 0.5rem;
`;

export const Input = styled.input`
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s;

  &:focus {
    outline: none;
    border-color: #6BB8E8;
  }

  &:disabled {
    background: #f7fafc;
    cursor: not-allowed;
  }
`;

export const Checkbox = styled.input`
  width: 1.25rem;
  height: 1.25rem;
  margin-right: 0.5rem;
  cursor: pointer;
`;

export const CheckboxLabel = styled.label`
  display: flex;
  align-items: center;
  font-size: 0.875rem;
  color: #4a5568;
  cursor: pointer;
`;

export const Button = styled.button`
  padding: 0.75rem 1.5rem;
  background: #6BB8E8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover:not(:disabled) {
    background: #5AA7D7;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

export const ButtonSecondary = styled(Button)`
  background: #e2e8f0;
  color: #4a5568;

  &:hover:not(:disabled) {
    background: #cbd5e0;
  }
`;

export const EmptyState = styled.div`
  text-align: center;
  padding: 3rem;
  color: #718096;
`;

export const LoadingSpinner = styled.div`
  text-align: center;
  padding: 3rem;
  font-size: 1.125rem;
  color: #6BB8E8;
`;

export const ErrorMessage = styled.div`
  padding: 1rem;
  background: #fee;
  color: #c33;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
`;

export const SuccessMessage = styled.div`
  padding: 1rem;
  background: #d4edda;
  color: #155724;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
`;
