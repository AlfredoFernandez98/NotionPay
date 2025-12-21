import styled from 'styled-components';

export const NotionLiteContainer = styled.div`
  width: 100%;
`;

export const HeroSection = styled.section`
  position: relative;
  width: 100%;
  min-height: 400px;
  margin-bottom: 48px;

  @media (max-width: 768px) {
    min-height: 300px;
  }
`;

export const HeroImage = styled.img`
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;

  @media (max-width: 768px) {
    height: 300px;
  }
`;

export const HeroContent = styled.div`
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  padding: 40px 60px;
  border-radius: 12px;
  backdrop-filter: blur(10px);

  @media (max-width: 768px) {
    padding: 30px 40px;
  }

  @media (max-width: 480px) {
    padding: 20px 30px;
    width: 90%;
  }
`;

export const HeroTitle = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 3rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px 0;

  @media (max-width: 768px) {
    font-size: 2.5rem;
  }

  @media (max-width: 480px) {
    font-size: 2rem;
  }
`;

export const HeroSubtitle = styled.p`
  font-size: 1.25rem;
  color: #4a5568;
  margin: 0;

  @media (max-width: 768px) {
    font-size: 1.125rem;
  }

  @media (max-width: 480px) {
    font-size: 1rem;
  }
`;

export const ContentSection = styled.section`
  max-width: 1100px;
  margin: 0 auto 60px auto;
  padding: 0 20px;
`;

export const SectionTitle = styled.h2`
  font-family: 'Georgia', serif;
  font-size: 2rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 24px 0;
  text-align: center;

  @media (max-width: 768px) {
    font-size: 1.75rem;
  }
`;

export const SectionText = styled.p`
  font-size: 1.125rem;
  color: #4a5568;
  line-height: 1.7;
  margin: 0 0 32px 0;
  text-align: center;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
`;

export const FeatureGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
  margin-top: 32px;

  @media (max-width: 640px) {
    grid-template-columns: 1fr;
  }
`;

export const FeatureCard = styled.div`
  background-color: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 32px 24px;
  text-align: center;
  transition: transform 0.2s ease;

  &:hover {
    transform: translateY(-2px);
  }
`;

export const FeatureIcon = styled.div`
  font-size: 3rem;
  margin-bottom: 16px;
`;

export const FeatureTitle = styled.h3`
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 12px 0;
`;

export const FeatureDescription = styled.p`
  font-size: 0.9375rem;
  color: #718096;
  line-height: 1.6;
  margin: 0;
`;

export const WelcomeSection = styled.section`
  max-width: 1100px;
  margin: 60px auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  align-items: center;
  text-align: center;
`;

export const WelcomeImage = styled.img`
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
`;

export const WelcomeContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 700px;
  text-align: center;
`;

export const WelcomeTitle = styled.h2`
  font-family: 'Georgia', serif;
  font-size: 2rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0;
  text-align: center;
`;

export const WelcomeText = styled.p`
  font-size: 1rem;
  color: #4a5568;
  line-height: 1.7;
  margin: 0;
  text-align: center;
`;

export const CTASection = styled.section`
  max-width: 1100px;
  margin: 80px auto 40px auto;
  padding: 60px 20px;
  text-align: center;
  background-color: #f7fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
`;

export const CTAText = styled.p`
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 24px 0;
  text-align: center;

  @media (max-width: 768px) {
    font-size: 1.25rem;
  }
`;
