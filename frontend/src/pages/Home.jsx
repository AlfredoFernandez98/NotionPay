import {
  HomeContainer,
  HeroSection,
  Title,
  Subtitle,
  Tagline,
} from './Home.styles';
import Button from '../components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../utils/routes';

const Home = () => {
  const navigate = useNavigate();

  return (
    <HomeContainer>
      <HeroSection>
        <Tagline>Smart. Fast. Secure.</Tagline>
        <Title>Global Payments Made Simple</Title>
        <Subtitle>
          NotionPay enables seamless international transactions with enterprise-grade 
          security. Move money across borders effortlessly.
        </Subtitle>
        <Button onClick={() => navigate(ROUTES.signup)}>Get Started</Button>
      </HeroSection>
    </HomeContainer>
  );
};

export default Home;
