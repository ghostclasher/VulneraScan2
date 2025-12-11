import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { CssBaseline } from '@mui/material';
import { ThemeContextProvider } from './context/ThemeContext';
import NavBar from './components/NavBar';
import Login from './pages/Login';
import SignUp from './pages/SignUp';
import Dashboard from './pages/Dashboard';
import FileDetails from './pages/FileDetails';
import Settings from './pages/Settings';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';
import { getToken } from './services/auth';
import Footer from './components/Footer';

function AppRoutes() {
  const PrivateRoute = ({ children }) => {
    const token = getToken();
    return token ? children : <Navigate to="/" replace />;
  };

  return (
    <>
      <NavBar />
      <main style={{ minHeight: '100vh', paddingTop: 80 }}>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/dashboard" element={<PrivateRoute><Dashboard /></PrivateRoute>} />
          <Route path="/file/:id" element={<PrivateRoute><FileDetails /></PrivateRoute>} />
          <Route path="/settings" element={<PrivateRoute><Settings /></PrivateRoute>} />
          <Route path="/profile" element={<PrivateRoute><Profile /></PrivateRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </>
  );
}

export default function App() {
  return (
    <ThemeContextProvider>
      <CssBaseline />
      <AppRoutes />
    </ThemeContextProvider>
  );
}
