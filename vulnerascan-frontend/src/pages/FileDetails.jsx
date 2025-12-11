import React, { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import {
  Box,
  Paper,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Button,
  Card,
  Grid,
  Chip,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  ToggleButton,
  ToggleButtonGroup,
  Alert,
} from '@mui/material';
import { motion } from 'framer-motion';
import DownloadIcon from '@mui/icons-material/Download';
import ErrorIcon from '@mui/icons-material/Error';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import BugReportIcon from '@mui/icons-material/BugReport';
import RefreshIcon from '@mui/icons-material/Refresh';
import api from '../services/api';
import { useSnackbar } from 'notistack';

export default function FileDetails() {
  const { id } = useParams();
  const loc = useLocation();
  const [result, setResult] = useState(loc.state?.result || null);
  const [scanner, setScanner] = useState(loc.state?.scanner || 'gitleaks');
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [fileInfo, setFileInfo] = useState(null);
  const [scannerResults, setScannerResults] = useState({
    gitleaks: null,
    trivy: null,
    semgrep: null,
    dependencycheck: null,
  });
  const [selectedScanner, setSelectedScanner] = useState('gitleaks');
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    fetchFileInfo();
    fetchAllScanResults();
  }, [id]);

  async function fetchFileInfo() {
    try {
      const filesRes = await api.get('/api/scanfiles/my-uploads');
      const files = Array.isArray(filesRes.data) ? filesRes.data : filesRes.data?.results || [];
      const file = files.find((f) => f.id === id);
      if (file) {
        setFileInfo(file);
      }
    } catch (e) {
      console.error('Error fetching file info:', e);
    }
  }

  async function fetchAllScanResults() {
    setLoading(true);
    const scanners = [
      { key: 'gitleaks', endpoint: '/api/gitleaks/findings' },
      { key: 'trivy', endpoint: '/api/trivy/findings' },
      { key: 'semgrep', endpoint: '/api/semgrep/findings' },
      { key: 'dependencycheck', endpoint: '/api/dependency-check/findings' },
    ];

    const results = {};

    for (const scan of scanners) {
      try {
        const res = await api.get(`${scan.endpoint}/${id}`);
        results[scan.key] = {
          scannedFile: res.data.scannedFile || fileInfo?.originalFileName,
          findings: Array.isArray(res.data) ? res.data : res.data.findings || [],
          timestamp: res.data.timestamp || new Date().toISOString(),
        };
        console.log(`${scan.key} results:`, results[scan.key]);
      } catch (e) {
        console.log(`No ${scan.key} results available`);
        results[scan.key] = null;
      }
    }

    setScannerResults(results);

    // Set initial result to first available scanner
    const firstAvailable = Object.keys(results).find((key) => results[key]);
    if (firstAvailable) {
      setResult(results[firstAvailable]);
      setSelectedScanner(firstAvailable);
      setScanner(firstAvailable);
    }

    setLoading(false);
  }

  async function runScan(scannerType) {
    setScanning(true);
    const scanPaths = {
      gitleaks: '/api/gitleaks/scan',
      trivy: '/api/trivy/scan',
      semgrep: '/api/semgrep/scan',
      dependencycheck: '/api/dependency-check/scan',
    };

    try {
      const scanPath = scanPaths[scannerType];
      console.log(`Starting ${scannerType} scan on file ${id}`);
      const res = await api.post(`${scanPath}/${id}`);
      console.log('Scan result:', res.data);

      // Update results
      setScannerResults((prev) => ({
        ...prev,
        [scannerType]: {
          scannedFile: res.data.scannedFile || fileInfo?.originalFileName,
          findings: Array.isArray(res.data.findings) ? res.data.findings : res.data.findings || [],
          timestamp: new Date().toISOString(),
        },
      }));

      setResult({
        scannedFile: res.data.scannedFile || fileInfo?.originalFileName,
        findings: Array.isArray(res.data.findings) ? res.data.findings : res.data.findings || [],
        timestamp: new Date().toISOString(),
      });
      setSelectedScanner(scannerType);
      enqueueSnackbar(`${scannerType} scan completed!`, { variant: 'success' });
    } catch (ex) {
      const msg = ex.response?.data?.message || ex.response?.data || ex.message;
      console.error('Scan error:', msg);
      enqueueSnackbar(`${scannerType} scan failed: ${msg}`, { variant: 'error' });
    } finally {
      setScanning(false);
    }
  }

  function downloadReport() {
    const blob = new Blob([JSON.stringify(result, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${scanner}-result-${id}.json`;
    a.click();
    URL.revokeObjectURL(url);
    enqueueSnackbar('Report downloaded', { variant: 'success' });
  }

  if (loading && !result) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  const hasFindings = result?.findings && result.findings.length > 0;

  return (
    <Box sx={{ pb: 4, px: 2 }}>
      {/* File Info Card */}
      {fileInfo && (
        <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }}>
          <Paper
            sx={{
              p: 2.5,
              mb: 3,
              background: (theme) =>
                theme.palette.mode === 'dark'
                  ? 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(255,0,110,0.05) 100%)'
                  : 'linear-gradient(135deg, rgba(0,212,255,0.08) 0%, rgba(255,0,110,0.03) 100%)',
              border: '1px solid rgba(0,212,255,0.2)',
            }}
          >
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={6}>
                <Box>
                  <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.6)', display: 'block' }}>
                    File Name
                  </Typography>
                  <Typography sx={{ fontWeight: 600, mb: 1 }}>
                    {fileInfo.originalFileName || fileInfo.name}
                  </Typography>
                  <Typography variant="caption" sx={{ color: 'rgba(255,255,255,0.6)', display: 'block' }}>
                    Size: {fileInfo.fileSize ? `${(fileInfo.fileSize / 1024).toFixed(2)} KB` : '-'}
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} sx={{ textAlign: { xs: 'left', sm: 'right' } }}>
                <Button
                  variant="contained"
                  startIcon={<DownloadIcon />}
                  onClick={downloadReport}
                  disabled={!result}
                  sx={{ mr: 1, mb: 1 }}
                >
                  Download Report
                </Button>
              </Grid>
            </Grid>
          </Paper>
        </motion.div>
      )}

      {/* Scanner Selector */}
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.1 }}>
        <Paper
          sx={{
            p: 2.5,
            mb: 3,
            background: (theme) =>
              theme.palette.mode === 'dark'
                ? 'linear-gradient(135deg, rgba(255,255,255,.05) 0%, rgba(255,255,255,.02) 100%)'
                : 'linear-gradient(135deg, rgba(255,255,255,.8) 0%, rgba(248,250,252,.95) 100%)',
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Available Scans
            </Typography>
            <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.6 }}>
              <Button
                size="small"
                startIcon={<RefreshIcon />}
                onClick={fetchAllScanResults}
                disabled={loading}
              >
                Refresh
              </Button>
            </motion.div>
          </Box>

          <ToggleButtonGroup
            value={selectedScanner}
            exclusive
            onChange={(e, val) => {
              if (val && scannerResults[val]) {
                setSelectedScanner(val);
                setScanner(val);
                setResult(scannerResults[val]);
              }
            }}
            fullWidth
            sx={{ mb: 2 }}
          >
            {Object.keys(scannerResults).map((key) => (
              <ToggleButton
                key={key}
                value={key}
                disabled={!scannerResults[key]}
                sx={{
                  textTransform: 'capitalize',
                  fontWeight: 600,
                  '&.Mui-selected': { background: 'linear-gradient(135deg, #00d4ff 0%, #0099cc 100%)' },
                }}
              >
                {key === 'dependencycheck' ? 'Dep-Check' : key.charAt(0).toUpperCase() + key.slice(1)}
              </ToggleButton>
            ))}
          </ToggleButtonGroup>

          <Grid container spacing={2}>
            {Object.keys(scannerResults).map((key) => (
              <Grid item xs={12} sm={6} md={3} key={key}>
                <motion.div whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.98 }}>
                  <Button
                    fullWidth
                    variant="outlined"
                    startIcon={<BugReportIcon />}
                    onClick={() => runScan(key)}
                    disabled={scanning}
                    sx={{
                      fontWeight: 600,
                      color: scannerResults[key] ? '#2ed573' : 'inherit',
                      borderColor: scannerResults[key] ? '#2ed573' : 'rgba(0,212,255,0.3)',
                    }}
                  >
                    {scanning ? `Scanning ${key}...` : `Re-scan ${key === 'dependencycheck' ? 'Dep-Check' : key}`}
                  </Button>
                </motion.div>
              </Grid>
            ))}
          </Grid>
        </Paper>
      </motion.div>

      {/* Results */}
      {result ? (
        <>
          {/* Stats Cards */}
          <Grid container spacing={2} sx={{ mb: 3 }}>
            <Grid item xs={12} sm={6} md={3}>
              <motion.div whileHover={{ y: -5 }}>
                <Card
                  sx={{
                    p: 2,
                    background: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'linear-gradient(135deg, rgba(255,71,87,0.1) 0%, rgba(255,71,87,0.05) 100%)'
                        : 'linear-gradient(135deg, rgba(255,71,87,0.08) 0%, rgba(255,71,87,0.03) 100%)',
                    border: '2px solid rgba(255,71,87,0.3)',
                  }}
                >
                  <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)', fontWeight: 600 }}>
                    Total Findings
                  </Typography>
                  <Typography variant="h4" sx={{ color: '#ff4757', fontWeight: 800 }}>
                    {result.findings?.length || 0}
                  </Typography>
                </Card>
              </motion.div>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <motion.div whileHover={{ y: -5 }}>
                <Card
                  sx={{
                    p: 2,
                    background: (theme) =>
                      theme.palette.mode === 'dark'
                        ? 'linear-gradient(135deg, rgba(46,213,115,0.1) 0%, rgba(46,213,115,0.05) 100%)'
                        : 'linear-gradient(135deg, rgba(46,213,115,0.08) 0%, rgba(46,213,115,0.03) 100%)',
                    border: '2px solid rgba(46,213,115,0.3)',
                  }}
                >
                  <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)', fontWeight: 600 }}>
                    Status
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
                    {hasFindings ? (
                      <>
                        <ErrorIcon sx={{ color: '#ff4757' }} />
                        <Typography sx={{ color: '#ff4757', fontWeight: 700 }}>Issues Found</Typography>
                      </>
                    ) : (
                      <>
                        <CheckCircleIcon sx={{ color: '#2ed573' }} />
                        <Typography sx={{ color: '#2ed573', fontWeight: 700 }}>Clean</Typography>
                      </>
                    )}
                  </Box>
                </Card>
              </motion.div>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <motion.div whileHover={{ y: -5 }}>
                <Card sx={{ p: 2, background: (theme) => theme.palette.mode === 'dark' ? 'linear-gradient(135deg, rgba(0,212,255,0.1) 0%, rgba(0,212,255,0.05) 100%)' : 'linear-gradient(135deg, rgba(0,212,255,0.08) 0%, rgba(0,212,255,0.03) 100%)', border: '2px solid rgba(0,212,255,0.3)' }}>
                  <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)', fontWeight: 600 }}>
                    Scanner
                  </Typography>
                  <Typography sx={{ color: '#00d4ff', fontWeight: 700, mt: 1, textTransform: 'capitalize' }}>
                    {selectedScanner === 'dependencycheck' ? 'Dep-Check' : selectedScanner}
                  </Typography>
                </Card>
              </motion.div>
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <motion.div whileHover={{ y: -5 }}>
                <Card sx={{ p: 2, background: (theme) => theme.palette.mode === 'dark' ? 'linear-gradient(135deg, rgba(255,165,0,0.1) 0%, rgba(255,165,0,0.05) 100%)' : 'linear-gradient(135deg, rgba(255,165,0,0.08) 0%, rgba(255,165,0,0.03) 100%)', border: '2px solid rgba(255,165,0,0.3)' }}>
                  <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)', fontWeight: 600 }}>
                    Last Scanned
                  </Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600, mt: 1 }}>
                    {result.timestamp ? new Date(result.timestamp).toLocaleString() : 'Just now'}
                  </Typography>
                </Card>
              </motion.div>
            </Grid>
          </Grid>

          {/* Findings Table */}
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.2 }}>
            <Paper sx={{ p: 3, overflowX: 'auto' }}>
              <Typography variant="h6" sx={{ mb: 2, fontWeight: 700 }}>
                Detailed Findings
              </Typography>
              {hasFindings ? (
                <Table>
                  <TableHead>
                    <TableRow sx={{ borderBottom: '2px solid rgba(0,212,255,0.2)' }}>
                      <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Rule/Vulnerability</TableCell>
                      <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Description</TableCell>
                      <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>File/Package</TableCell>
                      <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Severity</TableCell>
                      <TableCell sx={{ color: '#00d4ff', fontWeight: 700 }}>Details</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {result.findings.map((f, i) => (
                      <TableRow key={i} sx={{ '&:hover': { backgroundColor: 'rgba(0,212,255,0.05)' } }}>
                        <TableCell>
                          <Chip
                            label={f.rule || f.vulnerabilityId || f.ruleId || 'N/A'}
                            size="small"
                            color={f.severity === 'CRITICAL' || f.severity === 'HIGH' ? 'error' : 'warning'}
                            variant="outlined"
                          />
                        </TableCell>
                        <TableCell sx={{ maxWidth: 200, wordBreak: 'break-word' }}>
                          {f.description || f.title || f.message || '-'}
                        </TableCell>
                        <TableCell sx={{ fontFamily: 'monospace', fontSize: 12 }}>
                          {f.filePath || f.pkgName || f.target || '-'}
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={f.severity || 'MEDIUM'}
                            size="small"
                            color={
                              f.severity === 'CRITICAL' || f.severity === 'HIGH'
                                ? 'error'
                                : f.severity === 'MEDIUM'
                                ? 'warning'
                                : 'default'
                            }
                          />
                        </TableCell>
                        <TableCell sx={{ fontSize: 12 }}>
                          {f.startLine ? `Line ${f.startLine}` : '-'}
                          {f.installedVersion ? ` | Ver: ${f.installedVersion}` : ''}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              ) : (
                <Alert severity="success" sx={{ mt: 2 }}>
                  ✅ No findings detected. Your code looks secure!
                </Alert>
              )}
            </Paper>
          </motion.div>
        </>
      ) : (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <ErrorIcon sx={{ fontSize: 48, color: '#ff4757', mb: 2 }} />
          <Typography variant="h6" sx={{ mb: 1 }}>
            No scan results available
          </Typography>
          <Typography variant="body2" sx={{ color: 'rgba(255,255,255,0.6)', mb: 2 }}>
            Run a scan to view results for this file.
          </Typography>
          <Grid container spacing={2} justifyContent="center">
            {['gitleaks', 'trivy', 'semgrep', 'dependencycheck'].map((key) => (
              <Grid item xs={6} sm={3} key={key}>
                <Button
                  variant="contained"
                  onClick={() => runScan(key)}
                  disabled={scanning}
                  fullWidth
                >
                  Scan {key === 'dependencycheck' ? 'Dep-Check' : key}
                </Button>
              </Grid>
            ))}
          </Grid>
        </Paper>
      )}
    </Box>
  );
}