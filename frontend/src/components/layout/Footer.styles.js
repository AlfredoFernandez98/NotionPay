import styled from 'styled-components';
import { Link } from 'react-router-dom';

export const FooterContainer = styled.footer`
  background-color: #ffffff;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding: 24px 0;
`;

export const FooterInner = styled.div`
  max-width: 1100px;
  margin: 0 auto;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;

  @media (min-width: 640px) {
    flex-direction: row;
    justify-content: space-between;
  }
`;

export const FooterLinks = styled.div`
  display: flex;
  align-items: center;
  gap: 24px;
`;

export const FooterLink = styled(Link)`
  text-decoration: none;
  font-size: 0.875rem;
  color: #718096;
  transition: color 0.2s ease;

  &:hover {
    color: #4a5568;
  }
`;

export const Copyright = styled.p`
  font-size: 0.8125rem;
  color: #a0aec0;
  margin: 0;
`;
