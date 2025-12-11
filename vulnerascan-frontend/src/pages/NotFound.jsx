import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';

export default function NotFound() {
  const nav = useNavigate();
  return (
    <Box sx={{ textAlign: 'center', py: 10 }}>
      <motion.div initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ duration: 0.6 }}>
        <Typography variant="h1" sx={{ fontSize: 100, fontWeight: 800 }}>404</Typography>
        <Typography variant="h5" sx={{ mb: 3 }}>Page Not Found</Typography>
        <Button variant="contained" onClick={() => nav('/dashboard')}>
          Back to Dashboard
        </Button>
      </motion.div>
    </Box>
  );
}