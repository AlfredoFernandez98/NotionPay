import {
  AboutContainer,
  AboutHeader,
  AboutTitle,
  AboutSubtitle,
  AboutContent,
  AboutSection,
  SectionTitle,
  SectionText,
  Timeline,
  TimelineItem,
  TimelineYear,
  TimelineContent,
} from './About.styles';

const About = () => {
  return (
    <AboutContainer>
      <AboutHeader>
        <AboutTitle>About NotionPay</AboutTitle>
        <AboutSubtitle>
          Pioneering the future of global payments since 2018
        </AboutSubtitle>
      </AboutHeader>

      <AboutContent>
        <AboutSection>
          <SectionTitle>Our Story</SectionTitle>
          <SectionText>
            NotionPay was founded in 2018 with a simple yet ambitious mission: to make international
            payments as seamless as sending a text message. What started as a small team of fintech
            enthusiasts in Copenhagen has grown into a trusted payment platform serving thousands of
            businesses worldwide.
          </SectionText>
          <SectionText>
            Our founders, experienced in both technology and finance, recognized the challenges businesses
            face when managing cross-border transactions. They envisioned a platform that would eliminate
            complexity, reduce costs, and provide enterprise-grade security for companies of all sizes.
          </SectionText>
        </AboutSection>

        <AboutSection>
          <SectionTitle>Our Journey</SectionTitle>
          <Timeline>
            <TimelineItem>
              <TimelineYear>2018</TimelineYear>
              <TimelineContent>
                Founded in Copenhagen with a team of 5 passionate innovators
              </TimelineContent>
            </TimelineItem>
            <TimelineItem>
              <TimelineYear>2019</TimelineYear>
              <TimelineContent>
                Launched our first payment gateway, processing over €10M in the first year
              </TimelineContent>
            </TimelineItem>
            <TimelineItem>
              <TimelineYear>2021</TimelineYear>
              <TimelineContent>
                Expanded to 15 countries and introduced multi-currency support
              </TimelineContent>
            </TimelineItem>
            <TimelineItem>
              <TimelineYear>2023</TimelineYear>
              <TimelineContent>
                Reached 5,000+ business clients and processed over €1B in transactions
              </TimelineContent>
            </TimelineItem>
            <TimelineItem>
              <TimelineYear>2025</TimelineYear>
              <TimelineContent>
                Serving 150+ countries with 24/7 support and advanced fraud protection
              </TimelineContent>
            </TimelineItem>
          </Timeline>
        </AboutSection>

        <AboutSection>
          <SectionTitle>Our Mission</SectionTitle>
          <SectionText>
            At NotionPay, we believe that borders shouldn't limit business growth. Our mission is to
            empower companies to expand globally by providing a reliable, secure, and user-friendly
            payment infrastructure that removes the traditional barriers of international commerce.
          </SectionText>
        </AboutSection>

        <AboutSection>
          <SectionTitle>Why Choose Us</SectionTitle>
          <SectionText>
            We combine cutting-edge technology with a deep understanding of global financial regulations.
            Our platform offers real-time transaction processing, competitive exchange rates, and
            bank-level security, all while maintaining the simplicity our clients love. With NotionPay,
            you focus on growing your business while we handle the complexity of global payments.
          </SectionText>
        </AboutSection>
      </AboutContent>
    </AboutContainer>
  );
};

export default About;
