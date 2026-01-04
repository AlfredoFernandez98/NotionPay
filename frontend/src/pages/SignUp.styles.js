import styled from 'styled-components';

export const SignUpContainer = styled.div`
  display: flex;
  justify-content: center;
  padding: 48px 0;
`;

export const SignUpCard = styled.div`
  width: 100%;
  max-width: 400px;
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 40px 32px;
`;

export const SignUpHeader = styled.div`
  text-align: center;
  margin-bottom: 32px;
`;

export const SignUpTitle = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;
`;

export const SignUpSubtitle = styled.p`
  font-size: 0.9375rem;
  color: #718096;
  margin: 0;
`;

export const SignUpForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

export const LoginLink = styled.p`
  text-align: center;
  font-size: 0.875rem;
  color: #718096;
  margin: 24px 0 0 0;
  
  a {
    color: #6BB8E8;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
`;
