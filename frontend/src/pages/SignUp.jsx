import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
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
import apiFacade from '../util/apiFacade';

const SignUp = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [serialNumber, setSerialNumber] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    // Check if passwords match
    if (password !== confirmPassword) {
      setError('Passwords do not match. Please try again.');
      return;
    }
    
    // Check password length
    if (password.length < 8) {
      setError('Password must be at least 8 characters long.');
      return;
    }
    
    setLoading(true);

    try {
      // Call the register API
      const response = await apiFacade.register(email, password, companyName, serialNumber);
      
      // Show success message
      console.log('Registration successful!');
      console.log('Plan:', response.planName);
      console.log('Initial SMS credits:', response.initialSmsCredits);
      
      // Navigate to login page with success message (do NOT auto-login)
      navigate(ROUTES.login, { 
        state: { 
          registrationSuccess: true,
          email: response.email,
          companyName: companyName,
          planName: response.planName,
          initialSmsCredits: response.initialSmsCredits
        } 
      });
      
    } catch (err) {
      console.error('Registration error:', err);
      
      // Handle different error scenarios
      if (err.status === 400) {
        setError('Invalid input. Please check all fields and try again.');
      } else if (err.status === 403) {
        setError('Invalid serial number or email. Please verify your information.');
      } else if (err.status === 422) {
        setError('An account with this email already exists. Please login instead.');
      } else if (err.fullError) {
        // If fullError is a promise, resolve it
        try {
          const errorDetails = await err.fullError;
          setError(errorDetails.msg || 'Registration failed. Please try again.');
        } catch {
          setError('Registration failed. Please try again.');
        }
      } else if (err.message) {
        setError(err.message);
      } else {
        setError('Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <SignUpContainer>
      <SignUpCard>
        <SignUpHeader>
          <SignUpTitle>Create Account</SignUpTitle>
          <SignUpSubtitle>Join NotionPay today</SignUpSubtitle>
        </SignUpHeader>

        <SignUpForm onSubmit={handleSubmit}>
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
            placeholder="Create a password (min. 8 characters)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            disabled={loading}
          />
          <Input
            id="confirmPassword"
            label="Confirm Password"
            type="password"
            placeholder="Re-enter your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            disabled={loading}
          />
          <Input
            id="companyName"
            label="Company Name"
            type="text"
            placeholder="Enter your company name"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            required
            disabled={loading}
          />
          <Input
            id="serialNumber"
            label="Serial Number"
            type="text"
            placeholder="Enter your serial number"
            value={serialNumber}
            onChange={(e) => setSerialNumber(e.target.value)}
            required
            disabled={loading}
          />
          <Button type="submit" disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </Button>
        </SignUpForm>

        <LoginLink>
          Already have an account? <Link to={ROUTES.login}>Sign in</Link>
        </LoginLink>
      </SignUpCard>
    </SignUpContainer>
  );
};

export default SignUp;
