import { Link } from 'react-router-dom'
import '../styles/Header.css'

const Header = () => {
  return (
    <header className="header">
      <div className="header-container">
        <Link to="/" className="logo">
          💳 NotionPay
        </Link>
        <nav className="nav">
          <Link to="/">Home</Link>
          <Link to="/login">Login</Link>
          <Link to="/register">Register</Link>
          <Link to="/dashboard">Dashboard</Link>
        </nav>
      </div>
    </header>
  )
}

export default Header


