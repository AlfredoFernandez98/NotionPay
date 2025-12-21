import {
  NotionLiteContainer,
  HeroSection,
  HeroImage,
  HeroContent,
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
import hanwellImage from '../assets/hanwell-monitoring.png';
import welcomeImage from '../assets/notion-lite-welcome.png';

const NotionLite = () => {
  const navigate = useNavigate();

  return (
    <NotionLiteContainer>
      <HeroSection>
        <HeroImage src={hanwellImage} alt="Continuous Environmental Monitoring" />
        <HeroContent>
          <HeroTitle>Notion Lite</HeroTitle>
          <HeroSubtitle>
            Your answer to straightforward temperature monitoring
          </HeroSubtitle>
        </HeroContent>
      </HeroSection>

      <ContentSection>
        <SectionTitle>Continuous Environmental Monitoring Solutions</SectionTitle>
        <SectionText>
          Keep your sensitive products and materials safe from unexpected changes in environmental
          conditions with a reliable monitoring system. Notion Lite provides enterprise-grade
          monitoring in a simple, easy-to-use package.
        </SectionText>

        <FeatureGrid>
          <FeatureCard>
            <FeatureTitle>Dedicated Alarms</FeatureTitle>
            <FeatureDescription>
              Receive instant notifications when environmental conditions exceed your specified
              thresholds, ensuring rapid response to potential issues.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>FDA Compliance</FeatureTitle>
            <FeatureDescription>
              Complete data integrity and FDA 21 CFR Part 11 compliance built-in, meeting the
              strictest regulatory requirements.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Wide Range of Parameters</FeatureTitle>
            <FeatureDescription>
              Monitor temperature, CO2, humidity, pressure, and more with high-precision sensors
              and reliable data logging.
            </FeatureDescription>
          </FeatureCard>
        </FeatureGrid>
      </ContentSection>

      <WelcomeSection>
        <WelcomeImage src={welcomeImage} alt="Welcome to Notion Lite" />
        <WelcomeContent>
          <WelcomeTitle>Simple, Reliable, Professional</WelcomeTitle>
          <WelcomeText>
            Notion Lite combines professional-grade monitoring capabilities with an intuitive
            interface that anyone can use. No complex setup, no extensive training required - just
            reliable monitoring that works when you need it.
          </WelcomeText>
          <WelcomeText>
            Perfect for laboratories, pharmacies, hospitals, food storage facilities, and any
            environment where precise environmental monitoring is critical.
          </WelcomeText>
        </WelcomeContent>
      </WelcomeSection>

      <ContentSection>
        <SectionTitle>Why Choose Notion Lite?</SectionTitle>
        
        <FeatureGrid>
          <FeatureCard>
            <FeatureTitle>Quick Deployment</FeatureTitle>
            <FeatureDescription>
              Get up and running in minutes with our plug-and-play sensors and cloud-based platform.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Secure Data Storage</FeatureTitle>
            <FeatureDescription>
              Your monitoring data is encrypted and securely stored in the cloud with automatic backups.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Access Anywhere</FeatureTitle>
            <FeatureDescription>
              View real-time data and historical trends from any device, anywhere in the world.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Cost-Effective</FeatureTitle>
            <FeatureDescription>
              Professional monitoring without the enterprise price tag - perfect for small to
              medium-sized facilities.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>Automated Reports</FeatureTitle>
            <FeatureDescription>
              Generate compliance reports automatically, saving time and ensuring audit readiness.
            </FeatureDescription>
          </FeatureCard>

          <FeatureCard>
            <FeatureTitle>24/7 Support</FeatureTitle>
            <FeatureDescription>
              Our expert team is always available to help you with setup, troubleshooting, and
              optimization.
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
