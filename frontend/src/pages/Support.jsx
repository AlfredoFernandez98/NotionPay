import {
  SupportContainer,
  SupportHeader,
  SupportTitle,
  SupportSubtitle,
  SupportContent,
  SupportSection,
  ContactCard,
  ContactAvatar,
  ContactInfo,
  ContactName,
  ContactRole,
  ContactDetails,
  ContactItem,
  ContactLabel,
  ContactValue,
  InfoSection,
  InfoTitle,
  InfoText,
  FAQSection,
  FAQItem,
  FAQQuestion,
  FAQAnswer,
} from './Support.styles';

const Support = () => {
  return (
    <SupportContainer>
      <SupportHeader>
        <SupportTitle>Customer Support</SupportTitle>
        <SupportSubtitle>
          We're here to help you succeed with NotionPay
        </SupportSubtitle>
      </SupportHeader>

      <SupportContent>
        <SupportSection>
          <ContactCard>
            <ContactAvatar>J</ContactAvatar>
            <ContactInfo>
              <ContactName>Jon Anderson</ContactName>
              <ContactRole>Senior Support Specialist</ContactRole>
              <ContactDetails>
                <ContactItem>
                  <ContactLabel>Email:</ContactLabel>
                  <ContactValue>jon.anderson@notionpay.com</ContactValue>
                </ContactItem>
                <ContactItem>
                  <ContactLabel>Phone:</ContactLabel>
                  <ContactValue>+45 20 12 34 56</ContactValue>
                </ContactItem>
                <ContactItem>
                  <ContactLabel>Available:</ContactLabel>
                  <ContactValue>Monday - Friday, 9:00 AM - 5:00 PM CET</ContactValue>
                </ContactItem>
                <ContactItem>
                  <ContactLabel>Response Time:</ContactLabel>
                  <ContactValue>Within 24 hours</ContactValue>
                </ContactItem>
              </ContactDetails>
            </ContactInfo>
          </ContactCard>

          <InfoSection>
            <InfoTitle>About Jon</InfoTitle>
            <InfoText>
              Jon has been with NotionPay since 2020, bringing 8+ years of fintech support experience. 
              He specializes in payment integrations, API troubleshooting, and helping businesses 
              optimize their payment workflows.
            </InfoText>
            <InfoText>
              With expertise in technical implementations and compliance questions, Jon provides 
              clear, actionable solutions. Languages: English, Danish, and German.
            </InfoText>
          </InfoSection>
        </SupportSection>

        <SupportSection>
          <InfoTitle>How We Can Help</InfoTitle>
          <FAQSection>
            <FAQItem>
              <FAQQuestion>Technical Integration</FAQQuestion>
              <FAQAnswer>
                API integration guidance, code examples, and troubleshooting support.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Account Management</FAQQuestion>
              <FAQAnswer>
                Assistance with account setup, billing, and subscription management.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Payment Issues</FAQQuestion>
              <FAQAnswer>
                Quick investigation and resolution of transaction-related problems.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Compliance & Security</FAQQuestion>
              <FAQAnswer>
                Documentation and guidance on security measures and regulatory compliance.
              </FAQAnswer>
            </FAQItem>
          </FAQSection>
        </SupportSection>

        <SupportSection>
          <InfoTitle>Additional Support Channels</InfoTitle>
          <InfoText>
            <strong>Live Chat:</strong> Available during business hours for immediate assistance.
          </InfoText>
          <InfoText>
            <strong>Help Center:</strong> docs.notionpay.com for guides and tutorials.
          </InfoText>
          <InfoText>
            <strong>Emergency:</strong> Email support@notionpay.com with "URGENT" in subject.
          </InfoText>
        </SupportSection>
      </SupportContent>
    </SupportContainer>
  );
};

export default Support;
