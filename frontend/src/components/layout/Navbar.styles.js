import styled from 'styled-components';
import { NavLink as RouterNavLink } from 'react-router-dom';

export const NavbarContainer = styled.header`
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px;
  background-color: #ffffff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
`;

export const NavbarInner = styled.nav`
  height: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 32px;

  @media (max-width: 768px) {
    padding: 0 16px;
    display: flex;
  justify-content: space-between;
  }
`;

export const LogoLink = styled(RouterNavLink)`
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
`;

export const LogoImage = styled.img`
  height: 40px;
  width: auto;
`;

export const LogoText = styled.span`
  font-family: 'Georgia', serif;
  font-size: 1.25rem;
  font-weight: 600;
  color: #6BB8E8;
  letter-spacing: 0.5px;
`;

export const NavLinks = styled.ul`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 48px;
  list-style: none;
  margin: 0;
  padding: 0;

  @media (max-width: 640px) {
    gap: 16px;
  }
`;

export const NavItem = styled.li``;

export const StyledNavLink = styled(RouterNavLink)`
  text-decoration: none;
  font-size: 0.9375rem;
  font-weight: 500;
  color: #4a5568;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s ease, border-color 0.2s ease;

  &:hover {
    color: #2d3748;
  }

  &.active {
    color: #1a202c;
    border-bottom-color: #6BB8E8;
  }
`;

export const LoginWrapper = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 12px;
`;
