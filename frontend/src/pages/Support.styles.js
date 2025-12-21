import styled from 'styled-components';

export const SupportContainer = styled.div`
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
`;

export const SupportHeader = styled.header`
  text-align: center;
  margin-bottom: 60px;
`;

export const SupportTitle = styled.h1`
  font-family: 'Georgia', serif;
  font-size: 2.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px 0;

  @media (min-width: 640px) {
    font-size: 3rem;
  }
`;

export const SupportSubtitle = styled.p`
  font-size: 1.125rem;
  color: #718096;
  margin: 0;
`;

export const SupportContent = styled.div`
  display: flex;
  flex-direction: column;
  gap: 32px;
`;

export const SupportSection = styled.section`
  background-color: #ffffff;
  border-radius: 8px;
  padding: 32px;
  border: 1px solid #e2e8f0;
`;

export const ContactCard = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 24px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 2px solid #e2e8f0;
`;

export const ContactAvatar = styled.div`
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #6BB8E8;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 600;
  flex-shrink: 0;
`;

export const ContactInfo = styled.div`
  width: 100%;
  text-align: center;
`;

export const ContactName = styled.h2`
  font-size: 1.75rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;
  text-align: center;
`;

export const ContactRole = styled.p`
  font-size: 1rem;
  color: #718096;
  font-weight: 500;
  margin: 0 0 20px 0;
  text-align: center;
`;

export const ContactDetails = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 500px;
  margin: 0 auto;
`;

export const ContactItem = styled.div`
  display: flex;
  justify-content: center;
  gap: 8px;
  text-align: center;

  @media (max-width: 640px) {
    flex-direction: column;
    gap: 4px;
  }
`;

export const ContactLabel = styled.span`
  font-weight: 600;
  color: #4a5568;
  min-width: 130px;
`;

export const ContactValue = styled.span`
  color: #718096;
`;

export const InfoSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: center;
`;

export const InfoTitle = styled.h3`
  font-size: 1.5rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 16px 0;
  text-align: center;
`;

export const InfoText = styled.p`
  font-size: 1rem;
  color: #4a5568;
  line-height: 1.7;
  margin: 0;
  text-align: center;
`;

export const FAQSection = styled.div`
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 16px;
`;

export const FAQItem = styled.div`
  padding: 20px;
  background-color: #f7fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  text-align: center;
`;

export const FAQQuestion = styled.h4`
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 8px 0;
  text-align: center;
`;

export const FAQAnswer = styled.p`
  font-size: 0.9375rem;
  color: #4a5568;
  line-height: 1.6;
  margin: 0;
  text-align: center;
`;
