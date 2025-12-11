import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, Button, Box, Avatar, Menu, MenuItem, IconButton, Divider } from '@mui/material';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { getToken, removeToken } from '../services/auth';
import { motion } from 'framer-motion';
import { useTheme } from '../context/ThemeContext';
import SecurityIcon from '@mui/icons-material/Security';
import LogoutIcon from '@mui/icons-material/Logout';
import SettingsIcon from '@mui/icons-material/Settings';
import PersonIcon from '@mui/icons-material/Person';
import Brightness4Icon from '@mui/icons-material/Brightness4';
import Brightness7Icon from '@mui/icons-material/Brightness7';

export default function NavBar() {
  const nav = useNavigate();
  const loc = useLocation();
  const token = getToken();
  const { isDark, toggleTheme } = useTheme();
  const [anchorEl, setAnchorEl] = useState(null);

  function logout() {
    removeToken();
    setAnchorEl(null);
    nav('/');
  }

  const isActive = (path) => loc.pathname === path;
  const navItems = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Settings', path: '/settings' },
  ];

  return (
    <AppBar
      position="fixed"
      sx={{
        background: isDark
          ? 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)'
          : 'linear-gradient(135deg, rgba(0,212,255,0.15) 0%, rgba(255,0,110,0.1) 100%)',
        backdropFilter: 'blur(10px)',
        borderBottom: '1px solid rgba(0,212,255,0.2)',
        boxShadow: '0 8px 32px rgba(0,212,255,0.1)',
      }}
    >
      <Toolbar sx={{ display: 'flex', justifyContent: 'space-between', py: 1 }}>
        {/* Logo */}
        <motion.div initial={{ x: -20, opacity: 0 }} animate={{ x: 0, opacity: 1 }} transition={{ duration: 0.6 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.8 }}>
              <SecurityIcon sx={{ fontSize: 32, color: '#00d4ff' }} />
            </motion.div>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 800,
                fontSize: 24,
                background: 'linear-gradient(135deg, #00d4ff 0%, #ff006e 100%)',
                WebkitBackgroundClip: 'text',
                WebkitTextFillColor: 'transparent',
              }}
            >
              VulneraScan
            </Typography>
          </Box>
        </motion.div>

        {/* Navigation Items */}
        <Box sx={{ display: 'flex', gap: 3, alignItems: 'center' }}>
          {token &&
            navItems.map((item) => (
              <motion.div key={item.path} whileHover={{ y: -2 }}>
                <Button
                  component={Link}
                  to={item.path}
                  sx={{
                    color: isActive(item.path) ? '#00d4ff' : 'inherit',
                    fontWeight: 600,
                    borderBottom: isActive(item.path) ? '2px solid #00d4ff' : 'none',
                    pb: 1,
                    transition: 'all 0.3s ease',
                    '&:hover': { color: '#00d4ff' },
                  }}
                >
                  {item.label}
                </Button>
              </motion.div>
            ))}

          {/* Theme Toggle Button */}
          <motion.div whileHover={{ scale: 1.1 }} whileTap={{ scale: 0.95 }}>
            <IconButton
              onClick={toggleTheme}
              color="inherit"
              title={isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
              sx={{ fontSize: 24 }}
            >
              {isDark ? <Brightness7Icon /> : <Brightness4Icon />}
            </IconButton>
          </motion.div>

          {/* User Menu */}
          {token ? (
            <>
              <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                <Avatar
                  sx={{
                    background: 'linear-gradient(135deg, #00d4ff 0%, #ff006e 100%)',
                    cursor: 'pointer',
                    width: 40,
                    height: 40,
                    fontWeight: 700,
                  }}
                  onClick={(e) => setAnchorEl(e.currentTarget)}
                >
                  U
                </Avatar>
              </motion.div>

              <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={() => setAnchorEl(null)}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              >
                <MenuItem component={Link} to="/profile" onClick={() => setAnchorEl(null)}>
                  <PersonIcon sx={{ mr: 1 }} /> Profile
                </MenuItem>
                <MenuItem component={Link} to="/settings" onClick={() => setAnchorEl(null)}>
                  <SettingsIcon sx={{ mr: 1 }} /> Settings
                </MenuItem>
                <Divider />
                <MenuItem onClick={logout}>
                  <LogoutIcon sx={{ mr: 1 }} /> Logout
                </MenuItem>
              </Menu>
            </>
          ) : (
            <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
              <Button component={Link} to="/" variant="contained" size="small">
                Login
              </Button>
            </motion.div>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}