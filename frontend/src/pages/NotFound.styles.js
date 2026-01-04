import styled from 'styled-components';

export const NotFoundContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 80px 0;
`;

export const ErrorCode = styled.span`
  font-size: 5rem;
  font-weight: 700;
  color: rgba(107, 184, 232, 0.3);
  margin-bottom: 16px;
`;

export const Title = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 2rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 12px 0;
`;

export const Description = styled.p`
  font-size: 1rem;
  color: #718096;
  margin: 0 0 32px 0;
  max-width: 400px;
`;
