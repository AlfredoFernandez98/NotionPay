# NotionPay Frontend

Modern React 18 frontend for the NotionPay subscription and payment platform.

## 🚀 Tech Stack

- **React 18** - UI library
- **Vite** - Build tool and dev server
- **React Router** - Client-side routing
- **ESLint** - Code quality

## 📁 Project Structure

```
frontend/
├── src/
│   ├── assets/       # Static assets (icons, fonts, etc.)
│   ├── components/   # Reusable UI components
│   ├── images/       # Image files
│   ├── layouts/      # Layout components (headers, footers, sidebars)
│   ├── pages/        # Page components (routes)
│   ├── styles/       # Global styles and CSS modules
│   ├── util/         # Utility functions and helpers
│   ├── index.css     # Global CSS
│   └── main.jsx      # App entry point
├── public/           # Public static files
└── dist/             # Production build output
```

## 🛠️ Getting Started

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Runs the app at [http://localhost:3000](http://localhost:3000)

### Build

```bash
npm run build
```

Builds the app for production to the `dist` folder.

### Preview Production Build

```bash
npm run preview
```

### Lint

```bash
npm run lint
```

## 🔗 Backend Integration

The frontend is configured to proxy API requests to the Java backend running on `http://localhost:7070`.

All requests to `/api/*` will be forwarded to the backend automatically.

## 📝 Environment Variables

Create a `.env` file in the root directory:

```env
VITE_API_URL=http://localhost:7070
```

## 🎨 Development Guidelines

- Place reusable components in `src/components/`
- Place page components in `src/pages/`
- Place layout wrappers in `src/layouts/`
- Place utility functions in `src/util/`
- Keep styles modular in `src/styles/`
