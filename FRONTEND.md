# NotionPay Frontend

## Overview
React 18 application with Vite, styled-components, and Stripe Elements for payment processing.

## Technology Stack
- **Framework**: React 18.3.1
- **Build Tool**: Vite 6.0.1
- **Routing**: React Router DOM 7.1.1
- **Styling**: styled-components 6.1.14
- **UI Components**: Radix UI, Recharts
- **Payment**: Stripe React (@stripe/react-stripe-js)
- **State**: Zustand

## Project Structure
```
src/
├── pages/          # Main pages
├── components/     # Reusable components
│   ├── layout/     # Navbar, Footer, Layout
│   └── ui/         # Shadcn UI components
├── hooks/          # Custom React hooks
├── store/          # Zustand stores
├── util/           # API facade, helpers
└── utils/          # Routes, storage
```

## Key Pages

### Public Pages
- **Home** (`/`) - Landing page
- **About** (`/about`) - Company information
- **NotionLite** (`/notion-lite`) - Product showcase
- **Support** (`/support`) - Contact information
- **Login** (`/login`) - User authentication
- **SignUp** (`/signup`) - New user registration

### Protected Pages (require login)
- **Dashboard** (`/dashboard`) - Main user hub
- **Buy SMS** (`/buy-sms`) - Purchase SMS credits
- **Payment Methods** (`/payment-methods`) - Manage cards
- **Payments** (`/payments`) - Payment history

## Configuration

### Environment Variables
```env
VITE_API_URL=http://localhost:7070/api
```

### Installation & Running
```bash
cd frontend
npm install
npm run dev
```

Runs on: `http://localhost:3001`

## State Management

### Auth Store (`store/authStore.js`)
- User authentication state
- Login/logout functions
- Token management

### UI Store (`store/uiStore.js`)
- Loading states
- Error messages
- UI preferences

## API Integration

### API Facade (`util/apiFacade.js`)
Central API service handling all backend requests:
- Authentication (login, register, logout)
- Customer operations
- Payment processing
- Subscription management
- Product listing
- Receipt retrieval

### Local Storage
- `jwtToken` - JWT authentication token
- `userEmail` - Logged in user email
- `sessionId` - Current session ID
- `customerId` - Customer ID

## Key Features

### Dashboard
Displays:
- Customer profile
- Active subscription with all plans
- SMS balance
- Recent activities
- Payment methods
- Recent receipts

### Payment Flow
1. User selects SMS package
2. Choose saved payment method or enter new card
3. Stripe Elements handles card input
4. Payment processed via backend
5. SMS balance updated
6. Receipt generated

### Stripe Integration
- Stripe Elements for secure card input
- Test mode in development
- PCI-compliant (card data never touches our server)

## Styling

### Theme
- Primary color: #6BB8E8 (light blue)
- Font: System fonts (Apple, Segoe UI, etc.)
- Clean, minimalistic design
- Fully responsive

### Styled Components
Each page has companion `.styles.js` file:
- `Dashboard.jsx` → `Dashboard.styles.js`
- Consistent styling patterns
- Responsive breakpoints

## Routing (`utils/routes.js`)
```javascript
ROUTES = {
  home: '/',
  login: '/login',
  signup: '/signup',
  dashboard: '/dashboard',
  buySMS: '/buy-sms',
  paymentMethods: '/payment-methods',
  payments: '/payments',
  about: '/about',
  notionLite: '/notion-lite',
  support: '/support'
}
```

## Authentication Flow
1. User enters credentials on login page
2. API facade sends to `/api/auth/login`
3. Backend validates and returns JWT + customer data
4. Token stored in localStorage
5. Auth store updated
6. User redirected to dashboard
7. Protected routes check for valid token

## Development

### Build
```bash
npm run build
```
Output: `dist/` directory

### Preview Production Build
```bash
npm run preview
```

### Linting
```bash
npm run lint
```

## API Calls

All requests go through `util/apiFacade.js`:
- Automatic token injection in headers
- Error handling
- Response parsing
- Consistent fetch options

Example:
```javascript
const data = await apiFacade.getCustomerProfile(customerId);
```

## Stripe Testing
Test cards for development:
- Success: 4242 4242 4242 4242
- Decline: 4000 0000 0000 0002
- Any future expiry date, any CVC

## Browser Support
- Modern evergreen browsers
- Chrome, Firefox, Safari, Edge (latest versions)

