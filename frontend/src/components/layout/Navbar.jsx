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

const Navbar = () => {
  const navigate = useNavigate();

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
        </NavLinks>

        <LoginWrapper>
          <Button variant="secondary" onClick={() => navigate(ROUTES.signup)}>
            Sign Up
          </Button>
          <Button variant="outline" onClick={() => navigate(ROUTES.login)}>
            Login
          </Button>
        </LoginWrapper>
      </NavbarInner>
    </NavbarContainer>
  );
};

export default Navbar;
