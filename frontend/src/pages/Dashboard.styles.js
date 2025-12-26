import styled from 'styled-components';

export const DashboardContainer = styled.div`
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
  width: 100%;
`;

export const DashboardHeader = styled.div`
  margin-bottom: 40px;
`;

export const WelcomeText = styled.h1`
  font-size: 2rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;

  @media (min-width: 768px) {
    font-size: 2.5rem;
  }
`;

export const SubText = styled.p`
  font-size: 1rem;
  color: #718096;
  margin: 0;
`;

export const DashboardGrid = styled.div`
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;

  @media (min-width: 768px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
`;

export const Card = styled.div`
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
`;

export const CardHeader = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
`;

export const CardTitle = styled.h3`
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
`;

export const CardIcon = styled.span`
  font-size: 1.25rem;
`;

export const CardContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const InfoRow = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
`;

export const InfoLabel = styled.span`
  font-size: 0.875rem;
  color: #718096;
  font-weight: 500;
`;

export const InfoValue = styled.span`
  font-size: 0.9375rem;
  color: #1a202c;
  font-weight: 600;
`;

export const StatusBadge = styled.span`
  display: inline-block;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  
  ${props => {
    switch (props.status) {
      case 'ACTIVE':
        return `
          background-color: rgba(72, 187, 120, 0.1);
          color: #2f855a;
        `;
      case 'INACTIVE':
        return `
          background-color: rgba(245, 101, 101, 0.1);
          color: #c53030;
        `;
      case 'PENDING':
        return `
          background-color: rgba(237, 137, 54, 0.1);
          color: #c05621;
        `;
      default:
        return `
          background-color: rgba(107, 184, 232, 0.1);
          color: #6BB8E8;
        `;
    }
  }}
`;

export const DropdownButton = styled.button`
  width: 100%;
  padding: 12px 16px;
  background-color: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.9375rem;
  color: #1a202c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.2s ease;

  &:hover {
    background-color: #edf2f7;
    border-color: #6BB8E8;
  }

  &:focus {
    outline: none;
    border-color: #6BB8E8;
    box-shadow: 0 0 0 3px rgba(107, 184, 232, 0.1);
  }
`;

export const DropdownContent = styled.div`
  margin-top: 12px;
  padding: 16px;
  background-color: #f7fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
`;

export const DropdownItem = styled.div`
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;

  &:last-child {
    border-bottom: none;
  }
`;

export const EmptyState = styled.div`
  text-align: center;
  padding: 32px 16px;
  color: #718096;
  font-size: 0.875rem;
`;

export const LoadingSpinner = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #6BB8E8;
  font-size: 1rem;
`;

export const ErrorMessage = styled.div`
  padding: 16px;
  background-color: #fff5f5;
  border: 1px solid #feb2b2;
  border-radius: 8px;
  color: #c53030;
  font-size: 0.875rem;
  margin-bottom: 24px;
`;

export const ActivityItem = styled.div`
  padding: 12px;
  background-color: #f7fafc;
  border-radius: 8px;
  margin-bottom: 8px;
  border-left: 3px solid #6BB8E8;

  &:last-child {
    margin-bottom: 0;
  }
`;

export const ActivityType = styled.div`
  font-size: 0.875rem;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 4px;
`;

export const ActivityDate = styled.div`
  font-size: 0.75rem;
  color: #718096;
`;

export const PaymentItem = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f7fafc;
  border-radius: 8px;
  margin-bottom: 8px;

  &:last-child {
    margin-bottom: 0;
  }
`;

export const PaymentAmount = styled.span`
  font-size: 1rem;
  font-weight: 600;
  color: #6BB8E8;
`;

export const PaymentDate = styled.span`
  font-size: 0.75rem;
  color: #718096;
`;

export const ReceiptItem = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f7fafc;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #edf2f7;
  }

  &:last-child {
    margin-bottom: 0;
  }
`;

export const ViewButton = styled.button`
  padding: 6px 12px;
  background-color: #6BB8E8;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #5BA8D8;
  }

  &:focus {
    outline: none;
    box-shadow: 0 0 0 3px rgba(107, 184, 232, 0.2);
  }
`;

export const LargeCard = styled(Card)`
  @media (min-width: 1024px) {
    grid-column: span 2;
  }
`;
