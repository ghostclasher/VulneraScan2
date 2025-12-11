import React, { useState } from 'react';
import {
  Box,
  TextField,
  Button,
  Paper,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  Card,
  Container,
} from '@mui/material';
import { motion } from 'framer-motion';
import EmailIcon from '@mui/icons-material/Email';
import LockIcon from '@mui/icons-material/Lock';
import api from '../services/api';
import { saveToken } from '../services/auth';
import { Link, useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const nav = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  async function submit(e) {
    e?.preventDefault();
    setErr('');
    setLoading(true);
    try {
      // Correct endpoint from Swagger: POST /api/v1/auth/token
      const res = await api.post('/api/v1/auth/token', { email, password });

      console.log('Login response:', res.data);

      let token = res.data?.token || res.data?.accessToken || res.data?.jwtToken || res.data;

      if (typeof token === 'object' && token?.token) token = token.token;
      if (typeof token === 'object' && token?.accessToken) token = token.accessToken;

      if (!token || typeof token !== 'string') {
        throw new Error('Invalid token format received from server');
      }

      saveToken(token);
      console.log('Token saved:', token.substring(0, 20) + '...');

      enqueueSnackbar('Login successful!', { variant: 'success' });
      nav('/dashboard');
    } catch (ex) {
      const errMsg = ex.response?.data?.message || ex.response?.data?.error || ex.response?.data || ex.message;
      console.error('Login error:', errMsg);
      setErr(errMsg);
      enqueueSnackbar(errMsg, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', py: 4 }}>
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }} style={{ width: '100%' }}>
          <Paper sx={{ p: 4, background: 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)', border: '1px solid rgba(0,212,255,0.2)', backdropFilter: 'blur(10px)' }}>
            <Box sx={{ textAlign: 'center', mb: 3 }}>
              <Typography
                variant="h4"
                sx={{
                  fontWeight: 800,
                  background: 'linear-gradient(135deg, #00d4ff 0%, #ff006e 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  mb: 1,
                }}
              >
                VulneraScan
              </Typography>
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)' }}>
                Advanced Security Scanning Platform
              </Typography>
            </Box>

            {err && (
              <motion.div initial={{ x: -10 }} animate={{ x: 0 }}>
                <Alert severity="error" sx={{ mb: 2 }}>
                  {err}
                </Alert>
              </motion.div>
            )}

            <form onSubmit={submit}>
              <TextField
                label="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                fullWidth
                sx={{ mb: 2 }}
                disabled={loading}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <EmailIcon sx={{ color: '#00d4ff' }} />
                    </InputAdornment>
                  ),
                }}
              />
              <TextField
                label="Password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                fullWidth
                sx={{ mb: 3 }}
                disabled={loading}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LockIcon sx={{ color: '#00d4ff' }} />
                    </InputAdornment>
                  ),
                }}
              />
              <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                <Button
                  type="submit"
                  variant="contained"
                  fullWidth
                  disabled={loading || !email || !password}
                  sx={{ py: 1.5, fontSize: 16, fontWeight: 600 }}
                >
                  {loading ? <CircularProgress size={24} /> : 'Sign In'}
                </Button>
              </motion.div>
            </form>

            <Typography variant="caption" sx={{ display: 'block', mt: 2, textAlign: 'center', color: 'rgba(255,255,255,0.5)' }}>
              Use your backend credentials to login
            </Typography>

            <Typography sx={{ textAlign: 'center', mt: 3, color: 'rgba(255,255,255,0.6)' }}>
              Don't have an account?{' '}
              <Link to="/signup" style={{ color: '#00d4ff', fontWeight: 600, textDecoration: 'none' }}>
                Sign Up
              </Link>
            </Typography>
          </Paper>
        </motion.div>
      </Box>
    </Container>
  );
}