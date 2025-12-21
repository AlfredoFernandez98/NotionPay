import { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  LoginContainer,
  LoginCard,
  LoginHeader,
  LoginTitle,
  LoginSubtitle,
  LoginForm,
  ForgotPassword,
  SignupLink,
} from './Login.styles';
import Input from '../components/ui/Input';
import Button from '../components/ui/Button';
import { ROUTES } from '../utils/routes';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    // Login logic will be implemented here
    console.log('Login attempt:', { email, password });
  };

  return (
    <LoginContainer>
      <LoginCard>
        <LoginHeader>
          <LoginTitle>Welcome Back</LoginTitle>
          <LoginSubtitle>Sign in to your NotionPay account</LoginSubtitle>
        </LoginHeader>

        <LoginForm onSubmit={handleSubmit}>
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
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <ForgotPassword href="#">Forgot password?</ForgotPassword>
          <Button type="submit">Sign In</Button>
        </LoginForm>

        <SignupLink>
          Don't have an account? <Link to={ROUTES.signup}>Sign up</Link>
        </SignupLink>
      </LoginCard>
    </LoginContainer>
  );
};

export default Login;
