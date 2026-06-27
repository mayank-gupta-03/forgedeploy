import { useState, useEffect } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import RedirectHandler from './pages/RedirectHandler';
import Dashboard from './pages/Dashboard';
import { api, getToken, decodeToken, type User } from './services/api';

type View = 'LOGIN' | 'REGISTER' | 'DASHBOARD' | 'OAUTH_REDIRECT';

export default function App() {
  const [currentView, setCurrentView] = useState<View>('LOGIN');
  const [user, setUser] = useState<User | null>(null);

  // Check initial route and authentication state
  useEffect(() => {
    const handleRoute = () => {
      const path = window.location.pathname;
      
      if (path === '/oauth2/redirect') {
        setCurrentView('OAUTH_REDIRECT');
        return;
      }

      const savedToken = getToken();
      if (savedToken) {
        const decoded = decodeToken(savedToken);
        if (decoded) {
          setUser(decoded);
          setCurrentView('DASHBOARD');
          return;
        }
      }

      setCurrentView('LOGIN');
    };

    handleRoute();
    
    // Support browser back/forward buttons
    window.addEventListener('popstate', handleRoute);
    return () => window.removeEventListener('popstate', handleRoute);
  }, []);

  const handleLoginSuccess = (newToken: string) => {
    const decoded = decodeToken(newToken);
    if (decoded) {
      setUser(decoded);
      setCurrentView('DASHBOARD');
      // Clean up the URL in case it had OAuth redirect tokens
      if (window.location.pathname === '/oauth2/redirect') {
        window.history.replaceState({}, document.title, '/');
      }
    }
  };

  const handleLogout = () => {
    api.logout();
    setUser(null);
    setCurrentView('LOGIN');
    window.history.replaceState({}, document.title, '/');
  };

  const navigateToLogin = () => {
    setCurrentView('LOGIN');
    window.history.replaceState({}, document.title, '/');
  };

  const navigateToRegister = () => {
    setCurrentView('REGISTER');
  };

  switch (currentView) {
    case 'OAUTH_REDIRECT':
      return (
        <RedirectHandler 
          onLoginSuccess={handleLoginSuccess} 
          onNavigateToLogin={navigateToLogin} 
        />
      );
    case 'REGISTER':
      return (
        <Register 
          onNavigateToLogin={navigateToLogin} 
        />
      );
    case 'DASHBOARD':
      if (!user) return null;
      return (
        <Dashboard 
          user={user} 
          onLogout={handleLogout} 
        />
      );
    case 'LOGIN':
    default:
      return (
        <Login 
          onLoginSuccess={handleLoginSuccess} 
          onNavigateToRegister={navigateToRegister} 
        />
      );
  }
}
