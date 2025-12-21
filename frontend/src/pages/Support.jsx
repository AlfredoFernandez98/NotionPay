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
              Jon has been with NotionPay since 2020 and brings over 8 years of experience in
              fintech support. He specializes in payment integrations, API troubleshooting, and
              helping businesses optimize their payment workflows. Jon is passionate about
              providing clear, actionable solutions and ensuring every customer feels supported
              throughout their journey with NotionPay.
            </InfoText>
            <InfoText>
              With a background in computer science and extensive training in international
              payment regulations, Jon can assist with technical implementations, compliance
              questions, and general platform inquiries. He speaks English, Danish, and German
              fluently.
            </InfoText>
          </InfoSection>
        </SupportSection>

        <SupportSection>
          <InfoTitle>How We Can Help</InfoTitle>
          <FAQSection>
            <FAQItem>
              <FAQQuestion>Technical Integration</FAQQuestion>
              <FAQAnswer>
                Need help integrating our API? Jon can guide you through the process, provide
                code examples, and troubleshoot any issues you encounter.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Account Management</FAQQuestion>
              <FAQAnswer>
                Questions about your account, billing, or subscription? We'll help you manage
                your NotionPay account effectively.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Payment Issues</FAQQuestion>
              <FAQAnswer>
                Experiencing issues with transactions? We'll investigate and resolve any
                payment-related problems quickly.
              </FAQAnswer>
            </FAQItem>
            <FAQItem>
              <FAQQuestion>Compliance & Security</FAQQuestion>
              <FAQAnswer>
                Need information about our security measures or regulatory compliance? We'll
                provide detailed documentation and guidance.
              </FAQAnswer>
            </FAQItem>
          </FAQSection>
        </SupportSection>

        <SupportSection>
          <InfoTitle>Other Ways to Reach Us</InfoTitle>
          <InfoText>
            <strong>Live Chat:</strong> Available on our platform during business hours for
            immediate assistance.
          </InfoText>
          <InfoText>
            <strong>Help Center:</strong> Visit our comprehensive documentation at
            docs.notionpay.com for guides, tutorials, and FAQs.
          </InfoText>
          <InfoText>
            <strong>Emergency Support:</strong> For critical issues outside business hours,
            email support@notionpay.com with "URGENT" in the subject line.
          </InfoText>
        </SupportSection>
      </SupportContent>
    </SupportContainer>
  );
};

export default Support;
