import {
  NavbarContainer,
  NavbarInner,
  LogoLink,
  LogoImage,
  LogoText,
  NavLinks,
  NavItem,
  StyledNavLink,
  LoginWrapper,
} from './Navbar.styles';
import { ROUTES } from '../../utils/routes';
import { useNavigate } from 'react-router-dom';
import logo from '../../assets/notionPayLogo.png';
import Button from '../ui/Button';
import { useAuth } from '../../hooks/useAuth';
import apiFacade from '../../util/apiFacade';

const Navbar = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user, logout: authLogout } = useAuth();

  const handleLogout = async () => {
    try {
      // Call backend logout to deactivate session
      await apiFacade.logoutWithBackend();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear auth state and navigate to home
      authLogout();
      navigate(ROUTES.home);
    }
  };

  return (
    <NavbarContainer>
      <NavbarInner>
        <LogoLink to={ROUTES.home}>
          <LogoImage src={logo} alt="NotionPay Logo" />
          <LogoText>NotionPay</LogoText>
        </LogoLink>

        <NavLinks>
          <NavItem>
            <StyledNavLink to={ROUTES.about}>About</StyledNavLink>
          </NavItem>
          <NavItem>
            <StyledNavLink to={ROUTES.support}>Support</StyledNavLink>
          </NavItem>
          <NavItem>
            <StyledNavLink to={ROUTES.notionLite}>Notion Lite</StyledNavLink>
          </NavItem>
          {isAuthenticated && (
            <>
              <NavItem>
                <StyledNavLink to={ROUTES.dashboard}>Dashboard</StyledNavLink>
              </NavItem>
              <NavItem>
                <StyledNavLink to={ROUTES.buySMS}>Buy SMS</StyledNavLink>
              </NavItem>
              <NavItem>
                <StyledNavLink to={ROUTES.payments}>Payments</StyledNavLink>
              </NavItem>
              <NavItem>
                <StyledNavLink to={ROUTES.paymentMethods}>Cards</StyledNavLink>
              </NavItem>
            </>
          )}
        </NavLinks>

        <LoginWrapper>
          {isAuthenticated ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
              <span style={{ color: '#4a5568', fontSize: '14px' }}>
                {user?.email}
              </span>
              <Button variant="outline" onClick={handleLogout}>
                Logout
              </Button>
            </div>
          ) : (
            <Button variant="outline" onClick={() => navigate(ROUTES.login)}>
              Login
            </Button>
          )}
        </LoginWrapper>
      </NavbarInner>
    </NavbarContainer>
  );
};

export default Navbar;
