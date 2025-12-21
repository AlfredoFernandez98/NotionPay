import styled from 'styled-components';

export const HomeContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  flex: 1;
  width: 100%;
  padding: 40px 24px;
`;

export const HeroSection = styled.section`
  max-width: 800px;
  width: 100%;
  margin: 0 auto;
`;

export const Title = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 2.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px 0;
  line-height: 1.2;

  @media (min-width: 640px) {
    font-size: 3rem;
  }
`;

export const Subtitle = styled.p`
  font-size: 1.125rem;
  color: #718096;
  margin: 0 0 32px 0;
  line-height: 1.6;
`;

export const Tagline = styled.span`
  display: inline-block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #6BB8E8;
  margin-bottom: 16px;
  letter-spacing: 1px;
  text-transform: uppercase;
`;

export const FeaturesSection = styled.section`
  width: 100%;
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  
  @media (min-width: 768px) {
    grid-template-columns: repeat(3, 1fr);
  }
`;

export const FeatureCard = styled.div`
  background-color: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  padding: 32px 24px;
  text-align: left;
`;

export const FeatureIcon = styled.div`
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: rgba(107, 184, 232, 0.1);
  border-radius: 8px;
  margin-bottom: 16px;
  color: #6BB8E8;
  font-size: 1.5rem;
`;

export const FeatureTitle = styled.h3`
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;
`;

export const FeatureDescription = styled.p`
  font-size: 0.9375rem;
  color: #718096;
  margin: 0;
  line-height: 1.5;
`;
