import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  SignUpContainer,
  SignUpCard,
  SignUpHeader,
  SignUpTitle,
  SignUpSubtitle,
  SignUpForm,
  LoginLink,
} from './SignUp.styles';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { ROUTES } from '../utils/routes';

const SignUp = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [serialNumber, setSerialNumber] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    // Sign up logic will be implemented here
    console.log('Sign up attempt:', { email, password, companyName, serialNumber });
  };

  return (
    <SignUpContainer>
      <SignUpCard>
        <SignUpHeader>
          <SignUpTitle>Create Account</SignUpTitle>
          <SignUpSubtitle>Join NotionPay today</SignUpSubtitle>
        </SignUpHeader>

        <SignUpForm onSubmit={handleSubmit}>
          <Input
            id="email"
            label="Email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <Input
            id="password"
            label="Password"
            type="password"
            placeholder="Create a password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <Input
            id="companyName"
            label="Company Name"
            type="text"
            placeholder="Enter your company name"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
          />
          <Input
            id="serialNumber"
            label="Serial Number"
            type="number"
            placeholder="Enter your serial number"
            value={serialNumber}
            onChange={(e) => setSerialNumber(e.target.value)}
          />
          <Button type="submit">Create Account</Button>
        </SignUpForm>

        <LoginLink>
          Already have an account? <Link to={ROUTES.login}>Sign in</Link>
        </LoginLink>
      </SignUpCard>
    </SignUpContainer>
  );
};

export default SignUp;
