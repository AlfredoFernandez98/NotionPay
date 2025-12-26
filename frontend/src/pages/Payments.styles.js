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

export const FilterSection = styled.div`
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
`;

export const FilterButton = styled.button`
  padding: 0.5rem 1rem;
  background: ${props => props.active ? '#6BB8E8' : 'white'};
  color: ${props => props.active ? 'white' : '#4a5568'};
  border: 2px solid ${props => props.active ? '#6BB8E8' : '#e2e8f0'};
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #6BB8E8;
    background: ${props => props.active ? '#5AA7D7' : '#f7fafc'};
  }
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

export const PaymentHistoryItem = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 1rem;
  transition: all 0.2s;

  &:hover {
    border-color: #cbd5e0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  }

  &:last-child {
    margin-bottom: 0;
  }

  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
`;

export const PaymentInfo = styled.div`
  flex: 1;
`;

export const PaymentDescription = styled.div`
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.5rem;
`;

export const PaymentDate = styled.div`
  font-size: 0.875rem;
  color: #718096;
`;

export const PaymentAmount = styled.div`
  font-size: 1.25rem;
  font-weight: 700;
  color: #6BB8E8;
  white-space: nowrap;
`;

export const StatusBadge = styled.span`
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  white-space: nowrap;
  
  background: ${props => {
    switch (props.status) {
      case 'success': return '#d4edda';
      case 'pending': return '#fff3cd';
      case 'error': return '#f8d7da';
      default: return '#e2e8f0';
    }
  }};
  
  color: ${props => {
    switch (props.status) {
      case 'success': return '#155724';
      case 'pending': return '#856404';
      case 'error': return '#721c24';
      default: return '#4a5568';
    }
  }};
`;

export const ViewReceiptButton = styled.button`
  padding: 0.5rem 1rem;
  background: white;
  color: #6BB8E8;
  border: 2px solid #6BB8E8;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    background: #6BB8E8;
    color: white;
  }
`;

export const EmptyState = styled.div`
  text-align: center;
  padding: 3rem;
  color: #718096;

  p {
    margin: 0;
    font-size: 1.125rem;
  }
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
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
`;
