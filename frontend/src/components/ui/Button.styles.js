import styled, { css } from 'styled-components';

const primaryStyles = css`
  background-color: #6BB8E8;
  color: #ffffff;
  border: none;

  &:hover:not(:disabled) {
    background-color: #5BA8D8;
  }
`;

const secondaryStyles = css`
  background-color: #ffffff;
  color: #4a5568;
  border: 1px solid #e2e8f0;

  &:hover:not(:disabled) {
    background-color: #f7fafc;
  }
`;

const outlineStyles = css`
  background-color: transparent;
  color: #6BB8E8;
  border: 1px solid #6BB8E8;

  &:hover:not(:disabled) {
    background-color: #6BB8E8;
    color: #ffffff;
  }
`;

export const StyledButton = styled.button`
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 24px;
  font-size: 0.9375rem;
  font-weight: 500;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;

  ${({ variant }) => {
    switch (variant) {
      case 'secondary':
        return secondaryStyles;
      case 'outline':
        return outlineStyles;
      default:
        return primaryStyles;
    }
  }}

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &:focus {
    outline: none;
    box-shadow: 0 0 0 3px rgba(107, 184, 232, 0.2);
  }
`;
