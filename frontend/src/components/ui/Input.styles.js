import styled from 'styled-components';

export const InputWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;
`;

export const Label = styled.label`
  font-size: 0.875rem;
  font-weight: 500;
  color: #4a5568;
`;

export const StyledInput = styled.input`
  padding: 10px 14px;
  font-size: 0.9375rem;
  color: #2d3748;
  background-color: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 6px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;

  &::placeholder {
    color: #a0aec0;
  }

  &:hover {
    border-color: rgba(0, 0, 0, 0.2);
  }

  &:focus {
    outline: none;
    border-color: #6BB8E8;
    box-shadow: 0 0 0 3px rgba(107, 184, 232, 0.2);
  }

  &:disabled {
    background-color: #f7fafc;
    cursor: not-allowed;
  }
`;

export const ErrorText = styled.span`
  font-size: 0.8125rem;
  color: #e53e3e;
`;
