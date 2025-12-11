import React, { useState, useEffect } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  CircularProgress,
  Card,
  Grid,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  LinearProgress,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import { motion } from 'framer-motion';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import BugReportIcon from '@mui/icons-material/BugReport';
import RefreshIcon from '@mui/icons-material/Refresh';
import api from '../services/api';
import { useNavigate } from 'react-router-dom';
import { useSnackbar } from 'notistack';

export default function Dashboard() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [scanning, setScanining] = useState(null);
  const [error, setError] = useState('');
  const [openScanDialog, setOpenScanDialog] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [scannerType, setScannerType] = useState('gitleaks');
  const nav = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  // Exact backend endpoints
  const FILES_LIST_PATH = '/api/scanfiles/my-uploads';
  const UPLOAD_PATH = '/api/scanfiles/upload';

  const scannerPaths = {
    gitleaks: '/api/gitleaks/scan',
    trivy: '/api/trivy/scan',
    semgrep: '/api/semgrep/scan',
    dependencycheck: '/api/dependency-check/scan',
  };

  async function fetchFiles() {
    setLoading(true);
    setError('');
    try {
      const res = await api.get(FILES_LIST_PATH);
      console.log('Files response:', res.data);
      setFiles(Array.isArray(res.data) ? res.data : res.data?.results || []);
      enqueueSnackbar('Files loaded successfully', { variant: 'success' });
    } catch (e) {
      const msg = e.response?.data?.message || e.response?.data || e.message;
      console.error('Error fetching files:', msg);
      setError(msg);
      enqueueSnackbar(`Error: ${msg}`, { variant: 'error' });
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchFiles();
  }, []);

  async function onFile(e) {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    setUploading(true);
    const fd = new FormData();
    for (let i = 0; i < files.length; i++) {
      fd.append('files', files[i]);
    }

    try {
      const res = await api.post(UPLOAD_PATH, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      console.log('Upload response:', res.data);
      await fetchFiles();
      enqueueSnackbar('Files uploaded successfully', { variant: 'success' });
    } catch (ex) {
      const msg = ex.response?.data?.message || ex.response?.data || ex.message;
      console.error('Upload error:', msg);
      setError(msg);
      enqueueSnackbar(`Upload failed: ${msg}`, { variant: 'error' });
    } finally {
      setUploading(false);
      e.target.value = ''; // reset input
    }
  }

  async function runScan(id, scanner) {
    setScanining(`${id}-${scanner}`);
    const scanPath = scannerPaths[scanner] || scannerPaths.gitleaks;
    try {
      console.log(`Starting ${scanner} scan on file ${id}`);
      const res = await api.post(`${scanPath}/${id}`);
      console.log('Scan result:', res.data);
      enqueueSnackbar(`${scanner} scan completed`, { variant: 'success' });
      nav(`/file/${id}`, { state: { result: res.data, scanner } });
    } catch (ex) {
      const msg = ex.response?.data?.message || ex.response?.data || ex.message;
      console.error('Scan error:', msg);
      enqueueSnackbar(`${scanner} scan failed: ${msg}`, { variant: 'error' });
    } finally {
      setScanining(null);
      setOpenScanDialog(false);
    }
  }

  const fileCount = files.length;

  return (
    <Box sx={{ pb: 4 }}>
      {/* Header Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <motion.div whileHover={{ y: -5 }} transition={{ duration: 0.3 }}>
            <Card
              sx={{
                p: 2.5,
                background: 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(0,212,255,0.05) 100%)',
                border: '1px solid rgba(0,212,255,0.2)',
              }}
            >
              <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)' }}>
                Total Files
              </Typography>
              <Typography variant="h4" sx={{ color: '#00d4ff', fontWeight: 700 }}>
                {fileCount}
              </Typography>
            </Card>
          </motion.div>
        </Grid>
      </Grid>

      {/* Upload Section */}
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}>
        <Paper
          sx={{
            p: 3,
            mb: 4,
            background: 'linear-gradient(135deg, rgba(0,212,255,0.05) 0%, rgba(255,0,110,0.05) 100%)',
            border: '2px dashed rgba(0,212,255,0.3)',
            textAlign: 'center',
            cursor: 'pointer',
            transition: 'all 0.3s ease',
            '&:hover': {
              borderColor: '#00d4ff',
              background: 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)',
            },
          }}
        >
          <input
            id="file-upload"
            type="file"
            multiple
            style={{ display: 'none' }}
            onChange={onFile}
            disabled={uploading}
          />
          <label htmlFor="file-upload" style={{ cursor: 'pointer', display: 'block' }}>
            <motion.div whileHover={{ scale: 1.1 }}>
              <CloudUploadIcon sx={{ fontSize: 48, color: '#00d4ff', mb: 1 }} />
            </motion.div>
            <Typography variant="h6" sx={{ mb: 1 }}>
              {uploading ? 'Uploading...' : 'Upload Source Code'}
            </Typography>
            <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)' }}>
              Drag & drop files or click to select (supports .zip, .java, .py, etc.)
            </Typography>
            {uploading && <LinearProgress sx={{ mt: 2 }} />}
          </label>
        </Paper>
      </motion.div>

      {/* Files Table */}
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.2 }}>
        <Paper
          sx={{
            p: 3,
            background: 'rgba(22, 27, 51, 0.5)',
            border: '1px solid rgba(0,212,255,0.1)',
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Uploaded Files</Typography>
            <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.6 }}>
              <Button
                startIcon={<RefreshIcon />}
                size="small"
                onClick={() => fetchFiles()}
                disabled={loading}
              >
                Refresh
              </Button>
            </motion.div>
          </Box>

          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <CircularProgress />
            </Box>
          ) : (
            <>
              {error && (
                <Typography color="error" sx={{ mb: 2 }}>
                  {error}
                </Typography>
              )}
              <Table>
                <TableHead>
                  <TableRow sx={{ borderBottom: '2px solid rgba(0,212,255,0.2)' }}>
                    <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>File Name</TableCell>
                    <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Size</TableCell>
                    <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Uploaded</TableCell>
                    <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {files.map((f, idx) => (
                    <TableRow
                      key={f.id}
                      sx={{
                        '&:hover': { backgroundColor: 'rgba(0,212,255,0.05)' },
                      }}
                    >
                      <TableCell>{f.originalFileName || f.name || 'Unknown'}</TableCell>
                      <TableCell>
                        {f.fileSize ? `${(f.fileSize / 1024).toFixed(2)} KB` : '-'}
                      </TableCell>
                      <TableCell>
                        {f.uploadDate
                          ? new Date(f.uploadDate).toLocaleDateString()
                          : '-'}
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                            <Button
                              size="small"
                              variant="outlined"
                              onClick={() => nav(`/file/${f.id}`)}
                            >
                              View
                            </Button>
                          </motion.div>
                          <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                            <Button
                              size="small"
                              variant="contained"
                              startIcon={<BugReportIcon />}
                              onClick={() => {
                                setSelectedFile(f);
                                setScannerType('gitleaks');
                                setOpenScanDialog(true);
                              }}
                              disabled={scanning === `${f.id}-gitleaks`}
                            >
                              {scanning === `${f.id}-gitleaks` ? 'Scanning...' : 'Scan'}
                            </Button>
                          </motion.div>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              {files.length === 0 && (
                <Typography sx={{ textAlign: 'center', py: 4, color: 'rgba(255,255,255,0.5)' }}>
                  No files uploaded yet. Upload one to get started!
                </Typography>
              )}
            </>
          )}
        </Paper>
      </motion.div>

      {/* Scan Confirmation Dialog */}
      <Dialog open={openScanDialog} onClose={() => setOpenScanDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Run Security Scan</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2 }}>
            Scan <strong>{selectedFile?.originalFileName || selectedFile?.name}</strong> with which scanner?
          </Typography>
          <ToggleButtonGroup
            value={scannerType}
            exclusive
            onChange={(e, val) => val && setScannerType(val)}
            fullWidth
            sx={{ mb: 2 }}
          >
            <ToggleButton value="gitleaks">Gitleaks</ToggleButton>
            <ToggleButton value="trivy">Trivy</ToggleButton>
            <ToggleButton value="semgrep">Semgrep</ToggleButton>
            <ToggleButton value="dependencycheck">Dep-Check</ToggleButton>
          </ToggleButtonGroup>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenScanDialog(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={() => runScan(selectedFile?.id, scannerType)}
            disabled={scanning === `${selectedFile?.id}-${scannerType}`}
          >
            {scanning === `${selectedFile?.id}-${scannerType}` ? 'Scanning...' : 'Start Scan'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}