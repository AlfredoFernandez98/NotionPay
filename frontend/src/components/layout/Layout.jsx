import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Footer from './Footer';
import { PageWrapper, MainContent } from './Layout.styles';

const Layout = () => {
  return (
    <PageWrapper>
      <Navbar />
      <MainContent>
        <Outlet />
      </MainContent>
      <Footer />
    </PageWrapper>
  );
};

export default Layout;
