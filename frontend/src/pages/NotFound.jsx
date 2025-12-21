import { NotFoundContainer, ErrorCode, Title, Description } from './NotFound.styles';
import Button from '../components/ui/Button';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../utils/routes';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <NotFoundContainer>
      <ErrorCode>404</ErrorCode>
      <Title>Page Not Found</Title>
      <Description>
        The page you're looking for doesn't exist or has been moved.
      </Description>
      <Button onClick={() => navigate(ROUTES.home)}>Back to Home</Button>
    </NotFoundContainer>
  );
};

export default NotFound;
