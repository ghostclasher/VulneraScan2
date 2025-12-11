import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  TextField,
  Button,
  Card,
  Grid,
  Avatar,
  Divider,
  Alert,
  CircularProgress,
} from '@mui/material';
import { motion } from 'framer-motion';
import SaveIcon from '@mui/icons-material/Save';
import EditIcon from '@mui/icons-material/Edit';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import RefreshIcon from '@mui/icons-material/Refresh';
import api from '../services/api';
import { useSnackbar } from 'notistack';

export default function Profile() {
  const [profile, setProfile] = useState(null);
  const [editMode, setEditMode] = useState(false);
  const [tempProfile, setTempProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [stats, setStats] = useState({ totalScans: 0, filesUploaded: 0 });
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    fetchProfile();
    fetchStats();
  }, []);

  async function fetchProfile() {
    setLoading(true);
    try {
      const res = await api.get('/api/v1/auth/profile');
      console.log('Profile response:', res.data);

      const fullProfile = {
        name: res.data?.name || 'User Account',
        email: res.data?.email || 'user@example.com',
        role: res.data?.role || 'USER',
        joinDate: res.data?.joinDate || new Date().toLocaleDateString(),
      };

      setProfile(fullProfile);
      setTempProfile(fullProfile);
    } catch (e) {
      console.error('Error fetching profile:', e);
      const defaultProfile = {
        name: 'User Account',
        email: 'user@example.com',
        role: 'USER',
        joinDate: new Date().toLocaleDateString(),
      };
      setProfile(defaultProfile);
      setTempProfile(defaultProfile);
    } finally {
      setLoading(false);
    }
  }

  async function fetchStats() {
    try {
      // Fetch uploaded files count
      const filesRes = await api.get('/api/scanfiles/my-uploads');
      const filesList = Array.isArray(filesRes.data) ? filesRes.data : filesRes.data?.results || [];
      const filesUploaded = filesList.length;

      // Fetch scan results - try different endpoints
      let totalScans = 0;
      try {
        const scansRes = await api.get('/api/gitleaks/findings');
        totalScans = Array.isArray(scansRes.data) ? scansRes.data.length : 0;
      } catch (e) {
        console.log('Could not fetch scan counts');
      }

      setStats({ totalScans, filesUploaded });
      console.log('Stats fetched:', { totalScans, filesUploaded });
    } catch (e) {
      console.error('Error fetching stats:', e);
    }
  }

  const handleSave = async () => {
    setSaving(true);
    try {
      await api.put('/api/v1/auth/profile', tempProfile);
      setProfile(tempProfile);
      setEditMode(false);
      enqueueSnackbar('Profile updated successfully', { variant: 'success' });
    } catch (e) {
      console.log('Backend update failed, saving locally');
      setProfile(tempProfile);
      setEditMode(false);
      enqueueSnackbar('Profile saved locally', { variant: 'info' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!profile) {
    return (
      <Paper sx={{ p: 4, textAlign: 'center' }}>
        <Typography>Unable to load profile. Please try again.</Typography>
        <Button onClick={fetchProfile} sx={{ mt: 2 }}>
          Retry
        </Button>
      </Paper>
    );
  }

  return (
    <Box sx={{ pb: 4, px: 2 }}>
      <Grid container spacing={3}>
        {/* Profile Card */}
        <Grid item xs={12} md={4}>
          <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.6 }}>
            <Card
              sx={{
                p: 3,
                textAlign: 'center',
                background: (theme) =>
                  theme.palette.mode === 'dark'
                    ? 'linear-gradient(135deg, rgba(0,212,255,0.15) 0%, rgba(255,0,110,0.08) 100%)'
                    : 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)',
                border: '2px solid rgba(0,212,255,0.3)',
                boxShadow: (theme) =>
                  theme.palette.mode === 'dark'
                    ? '0 8px 32px rgba(0,212,255,0.15)'
                    : '0 4px 16px rgba(0,212,255,0.1)',
              }}
            >
              <Box sx={{ position: 'relative', display: 'inline-block', mb: 2 }}>
                <Avatar
                  sx={{
                    width: 120,
                    height: 120,
                    background: 'linear-gradient(135deg, #00d4ff 0%, #ff006e 100%)',
                    fontSize: 48,
                    fontWeight: 700,
                    cursor: 'pointer',
                    transition: 'all 0.3s ease',
                    boxShadow: '0 8px 24px rgba(0,212,255,0.4)',
                    '&:hover': {
                      transform: 'scale(1.08)',
                      boxShadow: '0 12px 32px rgba(0,212,255,0.6)',
                    },
                  }}
                >
                  {profile.name?.charAt(0).toUpperCase()}
                </Avatar>
                <motion.div
                  whileHover={{ scale: 1.15 }}
                  whileTap={{ scale: 0.9 }}
                  style={{
                    position: 'absolute',
                    bottom: 0,
                    right: 0,
                    background: 'linear-gradient(135deg, #00d4ff 0%, #33e0ff 100%)',
                    borderRadius: '50%',
                    padding: 10,
                    cursor: 'pointer',
                    boxShadow: '0 4px 12px rgba(0,212,255,0.5)',
                  }}
                >
                  <CameraAltIcon sx={{ color: '#000', fontSize: 20, fontWeight: 700 }} />
                </motion.div>
              </Box>

              <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5, fontSize: '1.3rem' }}>
                {profile.name}
              </Typography>
              <Typography
                variant="body2"
                sx={{
                  color: (theme) => theme.palette.text.secondary,
                  mb: 1,
                  wordBreak: 'break-all',
                }}
              >
                {profile.email}
              </Typography>

              <Divider sx={{ my: 2, opacity: 0.5 }} />

              <Box sx={{ textAlign: 'left', mb: 2 }}>
                <Typography
                  variant="caption"
                  sx={{
                    color: (theme) => theme.palette.text.secondary,
                    display: 'block',
                    fontSize: '0.75rem',
                  }}
                >
                  ROLE
                </Typography>
                <Typography sx={{ fontWeight: 700, color: '#00d4ff', mt: 0.5 }}>
                  {profile.role}
                </Typography>
              </Box>

              <Box sx={{ textAlign: 'left' }}>
                <Typography
                  variant="caption"
                  sx={{
                    color: (theme) => theme.palette.text.secondary,
                    display: 'block',
                    fontSize: '0.75rem',
                  }}
                >
                  MEMBER SINCE
                </Typography>
                <Typography sx={{ fontWeight: 600, mt: 0.5 }}>
                  {profile.joinDate}
                </Typography>
              </Box>

              <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }} style={{ marginTop: 16 }}>
                <Button
                  fullWidth
                  variant="contained"
                  startIcon={<EditIcon />}
                  onClick={() => {
                    setEditMode(!editMode);
                    setTempProfile(profile);
                  }}
                  sx={{
                    background: 'linear-gradient(135deg, #00d4ff 0%, #0099cc 100%)',
                    '&:hover': {
                      background: 'linear-gradient(135deg, #33e0ff 0%, #0066aa 100%)',
                    },
                  }}
                >
                  {editMode ? 'Cancel' : 'Edit Profile'}
                </Button>
              </motion.div>
            </Card>
          </motion.div>
        </Grid>

        {/* Edit Form & Stats */}
        <Grid item xs={12} md={8}>
          <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} transition={{ duration: 0.6 }}>
            <Paper
              sx={{
                p: 3,
                background: (theme) =>
                  theme.palette.mode === 'dark'
                    ? 'linear-gradient(135deg, rgba(0,212,255,0.05) 0%, rgba(255,0,110,0.02) 100%)'
                    : 'linear-gradient(135deg, rgba(0,212,255,0.08) 0%, rgba(255,0,110,0.03) 100%)',
                border: '1px solid rgba(0,212,255,0.2)',
                boxShadow: (theme) =>
                  theme.palette.mode === 'dark'
                    ? '0 8px 32px rgba(0,212,255,0.1)'
                    : '0 4px 16px rgba(0,0,0,0.05)',
              }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Typography variant="h6" sx={{ fontWeight: 700 }}>
                  {editMode ? 'Edit Profile Information' : 'Profile Information'}
                </Typography>
                {!editMode && (
                  <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.6 }}>
                    <Button
                      size="small"
                      startIcon={<RefreshIcon />}
                      onClick={fetchStats}
                      sx={{ color: '#00d4ff' }}
                    >
                      Refresh
                    </Button>
                  </motion.div>
                )}
              </Box>

              {editMode ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <TextField
                    label="Full Name"
                    value={tempProfile.name}
                    onChange={(e) => setTempProfile({ ...tempProfile, name: e.target.value })}
                    fullWidth
                    disabled={saving}
                  />
                  <TextField
                    label="Email Address"
                    type="email"
                    value={tempProfile.email}
                    onChange={(e) => setTempProfile({ ...tempProfile, email: e.target.value })}
                    fullWidth
                    disabled={saving}
                  />
                  <TextField
                    label="Role"
                    value={tempProfile.role}
                    disabled
                    fullWidth
                    helperText="Role cannot be changed"
                  />

                  <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                    <Button
                      variant="contained"
                      fullWidth
                      startIcon={<SaveIcon />}
                      onClick={handleSave}
                      disabled={saving}
                      sx={{
                        background: 'linear-gradient(135deg, #00d4ff 0%, #0099cc 100%)',
                        '&:hover': {
                          background: 'linear-gradient(135deg, #33e0ff 0%, #0066aa 100%)',
                        },
                      }}
                    >
                      {saving ? 'Saving...' : 'Save Changes'}
                    </Button>
                  </motion.div>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                  <Box>
                    <Typography
                      variant="caption"
                      sx={{
                        color: (theme) => theme.palette.text.secondary,
                        display: 'block',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        textTransform: 'uppercase',
                        letterSpacing: 0.5,
                        mb: 0.5,
                      }}
                    >
                      Full Name
                    </Typography>
                    <Typography sx={{ fontWeight: 600, fontSize: '1.05rem' }}>
                      {profile.name}
                    </Typography>
                  </Box>

                  <Divider sx={{ opacity: 0.3 }} />

                  <Box>
                    <Typography
                      variant="caption"
                      sx={{
                        color: (theme) => theme.palette.text.secondary,
                        display: 'block',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        textTransform: 'uppercase',
                        letterSpacing: 0.5,
                        mb: 0.5,
                      }}
                    >
                      Email Address
                    </Typography>
                    <Typography sx={{ fontWeight: 500, fontSize: '1.05rem', wordBreak: 'break-all' }}>
                      {profile.email}
                    </Typography>
                  </Box>

                  <Divider sx={{ opacity: 0.3 }} />

                  <Box>
                    <Typography
                      variant="caption"
                      sx={{
                        color: (theme) => theme.palette.text.secondary,
                        display: 'block',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        textTransform: 'uppercase',
                        letterSpacing: 0.5,
                        mb: 0.5,
                      }}
                    >
                      Account Role
                    </Typography>
                    <Typography sx={{ fontWeight: 600, color: '#00d4ff' }}>
                      {profile.role}
                    </Typography>
                  </Box>

                  <Divider sx={{ opacity: 0.3 }} />

                  <Box>
                    <Typography
                      variant="caption"
                      sx={{
                        color: (theme) => theme.palette.text.secondary,
                        display: 'block',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        textTransform: 'uppercase',
                        letterSpacing: 0.5,
                        mb: 0.5,
                      }}
                    >
                      Member Since
                    </Typography>
                    <Typography sx={{ fontWeight: 500 }}>
                      {profile.joinDate}
                    </Typography>
                  </Box>
                </Box>
              )}
            </Paper>
          </motion.div>

          {/* Account Stats */}
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <motion.div whileHover={{ y: -8 }} transition={{ type: 'spring', stiffness: 300 }}>
                <Card
                  sx={{
                    p: 3,
                    textAlign: 'center',
                    background: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'linear-gradient(135deg, rgba(0,212,255,0.15) 0%, rgba(51,224,255,0.05) 100%)'
                        : 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(51,224,255,0.05) 100%)',
                    border: '2px solid rgba(0,212,255,0.3)',
                    boxShadow: (theme) =>
                      theme.palette.mode === 'dark'
                        ? '0 8px 24px rgba(0,212,255,0.2)'
                        : '0 4px 12px rgba(0,212,255,0.1)',
                  }}
                >
                  <Typography
                    variant="body2"
                    sx={{
                      color: (theme) => theme.palette.text.secondary,
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      letterSpacing: 0.5,
                      fontSize: '0.75rem',
                      mb: 1,
                    }}
                  >
                    Total Scans Completed
                  </Typography>
                  <Typography variant="h3" sx={{ color: '#00d4ff', fontWeight: 800 }}>
                    {stats.totalScans}
                  </Typography>
                </Card>
              </motion.div>
            </Grid>
            <Grid item xs={12} sm={6}>
              <motion.div whileHover={{ y: -8 }} transition={{ type: 'spring', stiffness: 300 }}>
                <Card
                  sx={{
                    p: 3,
                    textAlign: 'center',
                    background: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'linear-gradient(135deg, rgba(255,0,110,0.15) 0%, rgba(255,51,136,0.05) 100%)'
                        : 'linear-gradient(135deg, rgba(255,0,110,0.1) 0%, rgba(255,51,136,0.05) 100%)',
                    border: '2px solid rgba(255,0,110,0.3)',
                    boxShadow: (theme) =>
                      theme.palette.mode === 'dark'
                        ? '0 8px 24px rgba(255,0,110,0.2)'
                        : '0 4px 12px rgba(255,0,110,0.1)',
                  }}
                >
                  <Typography
                    variant="body2"
                    sx={{
                      color: (theme) => theme.palette.text.secondary,
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      letterSpacing: 0.5,
                      fontSize: '0.75rem',
                      mb: 1,
                    }}
                  >
                    Files Uploaded
                  </Typography>
                  <Typography variant="h3" sx={{ color: '#ff006e', fontWeight: 800 }}>
                    {stats.filesUploaded}
                  </Typography>
                </Card>
              </motion.div>
            </Grid>
          </Grid>
        </Grid>
      </Grid>
    </Box>
  );
}