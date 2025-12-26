import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
import apiFacade from '../util/apiFacade';
import { setAuth } from '../store/authStore';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    console.log('=== LOGIN ATTEMPT ===');
    console.log('Email:', email);
    console.log('Backend URL:', import.meta.env.VITE_API_URL || 'http://localhost:7070/api');

    try {
      // Call the login API
      const response = await apiFacade.login(email, password);
      console.log('✅ Login response received:', response);
      
      // Only proceed if we have a valid token
      if (!response.token) {
        throw new Error('No token received from server');
      }
      
      // Store auth data in the store
      const user = {
        email: response.email,
        sessionId: response.sessionID,
      };
      
      setAuth(user, response.token);
      
      // Navigate to dashboard after successful login
      console.log('✅ Login successful! Redirecting to dashboard...');
      navigate('/dashboard');
      
    } catch (err) {
      console.error('❌ Login error:', err);
      
      // Handle error from backend
      if (err.fullError) {
        // If fullError is a promise, resolve it
        try {
          const errorDetails = await err.fullError;
          console.error('Error details:', errorDetails);
          setError(errorDetails.msg || 'Invalid email or password. Please try again.');
        } catch {
          setError('Invalid email or password. Please try again.');
        }
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Invalid email or password. Please try again.');
      }
      
      // Make sure navigation doesn't happen on error
      console.log('Login failed - staying on login page');
    } finally {
      setLoading(false);
    }
  };

  return (
    <LoginContainer>
      <LoginCard>
        <LoginHeader>
          <LoginTitle>Welcome Back</LoginTitle>
          <LoginSubtitle>Sign in to your NotionPay account</LoginSubtitle>
        </LoginHeader>

        <LoginForm onSubmit={handleSubmit}>
          {error && (
            <div style={{ 
              padding: '12px', 
              marginBottom: '16px', 
              backgroundColor: '#fee', 
              color: '#c33',
              borderRadius: '8px',
              fontSize: '14px'
            }}>
              {error}
            </div>
          )}
          
          <Input
            id="email"
            label="Email"
            type="email"
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={loading}
          />
          <Input
            id="password"
            label="Password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
          <ForgotPassword href="#">Forgot password?</ForgotPassword>
          <Button type="submit" disabled={loading}>
            {loading ? 'Signing In...' : 'Sign In'}
          </Button>
        </LoginForm>

        <SignupLink>
          Don't have an account? <Link to={ROUTES.signup}>Sign up</Link>
        </SignupLink>
      </LoginCard>
    </LoginContainer>
  );
};

export default Login;
