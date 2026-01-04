import styled from 'styled-components';

export const AboutContainer = styled.div`
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
`;

export const AboutHeader = styled.header`
  text-align: center;
  margin-bottom: 60px;
`;

export const AboutTitle = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 2.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px 0;

  @media (min-width: 640px) {
    font-size: 3rem;
  }
`;

export const AboutSubtitle = styled.p`
  font-size: 1.125rem;
  color: #6BB8E8;
  margin: 0;
  font-weight: 500;
`;

export const AboutContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 48px;
`;

export const AboutSection = styled.section`
  background-color: #ffffff;
  border-radius: 8px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  text-align: center;
`;

export const SectionTitle = styled.h2`
  font-family: 'Georgia', serif;
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 20px 0;
  text-align: center;
`;

export const SectionText = styled.p`
  font-size: 1rem;
  color: #4a5568;
  line-height: 1.7;
  margin: 0 auto 16px auto;
  text-align: center;
  max-width: 700px;

  &:last-child {
    margin-bottom: 0;
  }
`;

export const Timeline = styled.div`
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 24px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
`;

export const TimelineItem = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
  padding: 20px;
  background-color: #f7fafc;
  border-radius: 8px;
`;

export const TimelineYear = styled.div`
  font-size: 1.5rem;
  font-weight: 700;
  color: #1a202c;

  @media (max-width: 640px) {
    font-size: 1.25rem;
  }
`;

export const TimelineContent = styled.p`
  font-size: 1rem;
  color: #4a5568;
  line-height: 1.6;
  margin: 0;
  text-align: center;
`;
