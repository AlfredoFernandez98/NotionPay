import {
  StyledCard,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from './Card.styles';

const Card = ({ children, hoverable, padding, ...props }) => {
  return (
    <StyledCard hoverable={hoverable} padding={padding} {...props}>
      {children}
    </StyledCard>
  );
};

Card.Header = CardHeader;
Card.Title = CardTitle;
Card.Description = CardDescription;
Card.Content = CardContent;
Card.Footer = CardFooter;

export default Card;
