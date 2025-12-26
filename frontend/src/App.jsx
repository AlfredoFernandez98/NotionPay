import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Home from './pages/Home';
import About from './pages/About';
import Support from './pages/Support';
import NotionLite from './pages/NotionLite';
import Login from './pages/Login';
import SignUp from './pages/SignUp';
import Dashboard from './pages/Dashboard';
import PaymentMethods from './pages/PaymentMethods';
import BuySMS from './pages/BuySMS';
import Payments from './pages/Payments';
import NotFound from './pages/NotFound';
import { ROUTES } from './utils/routes';

const App = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path={ROUTES.home} element={<Home />} />
          <Route path={ROUTES.about} element={<About />} />
          <Route path={ROUTES.support} element={<Support />} />
          <Route path={ROUTES.notionLite} element={<NotionLite />} />
          <Route path={ROUTES.login} element={<Login />} />
          <Route path={ROUTES.signup} element={<SignUp />} />
          <Route path={ROUTES.dashboard} element={<Dashboard />} />
          <Route path={ROUTES.paymentMethods} element={<PaymentMethods />} />
          <Route path={ROUTES.buySMS} element={<BuySMS />} />
          <Route path={ROUTES.payments} element={<Payments />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
