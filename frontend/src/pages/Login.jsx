import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
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
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  // Check for registration success message
  useEffect(() => {
    if (location.state?.registrationSuccess) {
      const { email: registeredEmail, companyName, planName, initialSmsCredits } = location.state;
      setEmail(registeredEmail || '');
      setSuccess(
        `Account created successfully! Welcome ${companyName}! You have been subscribed to ${planName} with ${initialSmsCredits} SMS credits. Please login to continue.`
      );
      
      // Clear the state so message doesn't show again on refresh
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      // Call the login API
      const response = await apiFacade.login(email, password);
      
      // Only proceed if we have a valid token
      if (!response.token) {
        throw new Error('No token received from server');
      }
      
      // Store auth data in the store
      const user = {
        email: response.email,
        sessionId: response.sessionID,
        customerId: response.customerId,
      };
      
      setAuth(user, response.token);
      
      // Navigate to dashboard after successful login
      navigate('/dashboard');
      
    } catch (err) {
      console.error('Login error:', err);
      
      // Handle error from backend
      if (err.fullError) {
        // If fullError is a promise, resolve it
        try {
          const errorDetails = await err.fullError;
          setError(errorDetails.msg || 'Invalid email or password. Please try again.');
        } catch {
          setError('Invalid email or password. Please try again.');
        }
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Invalid email or password. Please try again.');
      }
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
          {success && (
            <div style={{ 
              padding: '16px', 
              marginBottom: '16px', 
              backgroundColor: '#d4edda', 
              color: '#155724',
              border: '1px solid #c3e6cb',
              borderRadius: '8px',
              fontSize: '14px',
              lineHeight: '1.5'
            }}>
              <strong>✓ Success!</strong>
              <br />
              {success}
            </div>
          )}
          
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
