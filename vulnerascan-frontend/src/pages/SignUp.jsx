import React, { useState } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Container,
  Alert,
  CircularProgress,
} from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import SecurityIcon from '@mui/icons-material/Security';
import api from '../services/api';
import { useSnackbar } from 'notistack';

export default function SignUp() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [err, setErr] = useState('');
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  async function submit(e) {
    e?.preventDefault();
    setErr('');

    // Validation
    if (!name.trim()) {
      setErr('Full name is required');
      return;
    }
    if (!email.trim()) {
      setErr('Email is required');
      return;
    }
    if (!password) {
      setErr('Password is required');
      return;
    }
    if (password !== confirmPassword) {
      setErr('Passwords do not match');
      return;
    }
    if (password.length < 6) {
      setErr('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    try {
      // Correct endpoint from Swagger: POST /api/v1/auth/users/add
      const res = await api.post('/api/v1/auth/users/add', { 
        name, 
        email, 
        password 
      });
      
      console.log('Sign up response:', res.data);
      enqueueSnackbar('Account created successfully! Redirecting to login...', { variant: 'success' });
      
      setTimeout(() => {
        nav('/');
      }, 2000);
    } catch (ex) {
      const errMsg = ex.response?.data?.message || ex.response?.data?.error || ex.response?.data || ex.message;
      console.error('Sign up error:', errMsg);
      setErr(errMsg || 'Sign up failed. Please try again.');
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          py: 4,
        }}
      >
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          style={{ width: '100%' }}
        >
          <Paper
            sx={{
              p: 4,
              background: 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)',
              border: '1px solid rgba(0,212,255,0.2)',
              backdropFilter: 'blur(10px)',
            }}
          >
            {/* Header */}
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1, mb: 3 }}>
              <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.8 }}>
                <SecurityIcon sx={{ fontSize: 32, color: '#00d4ff' }} />
              </motion.div>
              <Typography
                variant="h5"
                sx={{
                  fontWeight: 800,
                  background: 'linear-gradient(135deg, #00d4ff 0%, #ff006e 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}
              >
                VulneraScan
              </Typography>
            </Box>

            <Typography variant="h6" sx={{ textAlign: 'center', mb: 3, fontWeight: 700 }}>
              Create Account
            </Typography>

            {err && (
              <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
                <Alert severity="error" sx={{ mb: 2 }} onClose={() => setErr('')}>
                  {err}
                </Alert>
              </motion.div>
            )}

            <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                label="Full Name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                fullWidth
                disabled={loading}
                autoFocus
                placeholder="Enter your full name"
              />

              <TextField
                label="Email Address"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                fullWidth
                disabled={loading}
                placeholder="Enter your email"
              />

              <TextField
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                fullWidth
                disabled={loading}
                helperText="At least 6 characters"
                placeholder="Enter password"
              />

              <TextField
                label="Confirm Password"
                type="password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                fullWidth
                disabled={loading}
                placeholder="Confirm password"
              />

              <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={loading}
                  sx={{ mt: 2 }}
                >
                  {loading ? <CircularProgress size={24} /> : 'Sign Up'}
                </Button>
              </motion.div>
            </form>

            <Typography sx={{ textAlign: 'center', mt: 3, color: 'rgba(255,255,255,0.6)' }}>
              Already have an account?{' '}
              <Link to="/" style={{ color: '#00d4ff', fontWeight: 600, textDecoration: 'none' }}>
                Login
              </Link>
            </Typography>
          </Paper>
        </motion.div>
      </Box>
    </Container>
  );
}