import styled from 'styled-components';

export const StyledCard = styled.div`
  background-color: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  padding: ${({ padding }) => padding || '24px'};
  transition: box-shadow 0.2s ease;

  ${({ hoverable }) =>
    hoverable &&
    `
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }
  `}
`;

export const CardHeader = styled.div`
  margin-bottom: 16px;
`;

export const CardTitle = styled.h3`
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 4px 0;
`;

export const CardDescription = styled.p`
  font-size: 0.875rem;
  color: #718096;
  margin: 0;
`;

export const CardContent = styled.div``;

export const CardFooter = styled.div`
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
`;
