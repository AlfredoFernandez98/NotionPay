import {
  FooterContainer,
  FooterInner,
  FooterLinks,
  FooterLink,
  Copyright,
} from './Footer.styles';

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <FooterContainer>
      <FooterInner>
        <FooterLinks>
          <FooterLink to="#">Privacy</FooterLink>
          <FooterLink to="#">Terms</FooterLink>
          <FooterLink to="#">Support</FooterLink>
        </FooterLinks>
        <Copyright>© {currentYear} NotionPay</Copyright>
      </FooterInner>
    </FooterContainer>
  );
};

export default Footer;
