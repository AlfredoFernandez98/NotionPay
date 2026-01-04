import {
  NotionLiteContainer,
  HeroSection,
  HeroImage,
  HeroTitle,
  HeroSubtitle,
  ContentSection,
  SectionTitle,
  SectionText,
  FeatureGrid,
  FeatureCard,
  FeatureTitle,
  FeatureDescription,
  WelcomeSection,
  WelcomeImage,
  WelcomeContent,
  WelcomeTitle,
  WelcomeText,
  CTASection,
  CTAText,
} from './NotionLite.styles';
import Button from '../components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../utils/routes';
import ellabImage from '../assets/Ellab.jpg';
import welcomeImage from '../assets/notion-lite-welcome.png';

const NotionLite = () => {
  const navigate = useNavigate();

  return (
    <NotionLiteContainer>
      <HeroSection>
        <HeroImage src={ellabImage} alt="Ellab Environmental Monitoring" />
      </HeroSection>

      <ContentSection>
          <HeroTitle>Notion Lite</HeroTitle>
          <HeroSubtitle>
            Your answer to straightforward temperature monitoring
          </HeroSubtitle>
      </ContentSection>

      <ContentSection>
        <SectionTitle>Professional Environmental Monitoring</SectionTitle>
        <SectionText>
          Keep your sensitive products and materials safe with reliable monitoring. 
          Notion Lite provides enterprise-grade monitoring in a simple package.
        </SectionText>

        <FeatureGrid>
          <FeatureCard>
            <FeatureTitle>Instant Alerts</FeatureTitle>
            <FeatureDescription>
              Receive immediate notifications when conditions exceed your thresholds.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>FDA Compliant</FeatureTitle>
            <FeatureDescription>
              Complete data integrity and FDA 21 CFR Part 11 compliance built-in.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Multiple Parameters</FeatureTitle>
            <FeatureDescription>
              Monitor temperature, CO2, humidity, pressure with precision sensors.
            </FeatureDescription>
          </FeatureCard>
        </FeatureGrid>
      </ContentSection>

      <WelcomeSection>
        <WelcomeImage src={welcomeImage} alt="Welcome to Notion Lite" />
        <WelcomeContent>
          <WelcomeTitle>Simple. Reliable. Professional.</WelcomeTitle>
          <WelcomeText>
            Professional monitoring with an intuitive interface. No complex setup or 
            extensive training required.
          </WelcomeText>
          <WelcomeText>
            Perfect for laboratories, pharmacies, hospitals, and food storage facilities.
          </WelcomeText>
        </WelcomeContent>
      </WelcomeSection>

      <ContentSection>
        <SectionTitle>Key Benefits</SectionTitle>
        
        <FeatureGrid>
          <FeatureCard>
            <FeatureTitle>Quick Setup</FeatureTitle>
            <FeatureDescription>
              Plug-and-play sensors with cloud-based platform.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Secure Storage</FeatureTitle>
            <FeatureDescription>
              Encrypted data with automatic backups.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Remote Access</FeatureTitle>
            <FeatureDescription>
              Monitor from any device, anywhere.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Cost-Effective</FeatureTitle>
            <FeatureDescription>
              Professional monitoring at an affordable price.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Automated Reports</FeatureTitle>
            <FeatureDescription>
              Compliance reports generated automatically.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>24/7 Support</FeatureTitle>
            <FeatureDescription>
              Expert assistance whenever you need it.
            </FeatureDescription>
          </FeatureCard>
        </FeatureGrid>
      </ContentSection>

      <CTASection>
        <CTAText>Ready to secure your environment with Notion Lite?</CTAText>
        <Button onClick={() => navigate(ROUTES.signup)}>Get Started Today</Button>
      </CTASection>
    </NotionLiteContainer>
  );
};

export default NotionLite;
