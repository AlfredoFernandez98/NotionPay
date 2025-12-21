import { InputWrapper, Label, StyledInput, ErrorText } from './Input.styles';

const Input = ({ label, error, id, ...props }) => {
  return (
    <InputWrapper>
      {label && <Label htmlFor={id}>{label}</Label>}
      <StyledInput id={id} {...props} />
      {error && <ErrorText>{error}</ErrorText>}
    </InputWrapper>
  );
};

export default Input;
