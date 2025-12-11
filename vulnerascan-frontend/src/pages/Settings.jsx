import React, { useState } from 'react';
import { Paper, TextField, Button, Typography } from '@mui/material';

export default function Settings(){
  const [apiBase, setApiBase] = useState(localStorage.getItem('VITE_API_BASE_URL') || import.meta.env.VITE_API_BASE_URL || '');
  const [authPath, setAuthPath] = useState(localStorage.getItem('VAUTH_PATH') || import.meta.env.VITE_AUTH_TOKEN_PATH || '/api/v1/auth/token');
  const [filesPath, setFilesPath] = useState(localStorage.getItem('VFILES_PATH') || localStorage.getItem('VFILES_PATH') || '');
  const [uploadPath, setUploadPath] = useState(localStorage.getItem('VUPLOAD_PATH') || '');

  function save(){
    if (apiBase) localStorage.setItem('VITE_API_BASE_URL', apiBase);
    if (authPath) localStorage.setItem('VAUTH_PATH', authPath);
    if (filesPath) localStorage.setItem('VFILES_PATH', filesPath);
    if (uploadPath) localStorage.setItem('VUPLOAD_PATH', uploadPath);
    alert('Settings saved to localStorage. Restart frontend if you changed API base.');
  }

  return (
    <Paper sx={{ p:3 }}>
      <Typography variant="h6">Settings</Typography>
      <TextField fullWidth label="API Base URL" value={apiBase} onChange={e=>setApiBase(e.target.value)} sx={{ mt:2 }} />
      <TextField fullWidth label="Auth Token Path" value={authPath} onChange={e=>setAuthPath(e.target.value)} sx={{ mt:2 }} />
      <TextField fullWidth label="Files List Path" value={filesPath} onChange={e=>setFilesPath(e.target.value)} sx={{ mt:2 }} />
      <TextField fullWidth label="File Upload Path" value={uploadPath} onChange={e=>setUploadPath(e.target.value)} sx={{ mt:2 }} />
      <Button variant="contained" sx={{ mt:2 }} onClick={save}>Save</Button>
      <Typography variant="body2" sx={{ mt:2, color:'text.secondary' }}>
        You can keep backend untouched. Enter the exact endpoint paths your backend uses.
      </Typography>
    </Paper>
  );
}