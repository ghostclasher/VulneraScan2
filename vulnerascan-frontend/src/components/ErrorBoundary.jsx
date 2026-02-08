import React from 'react';
import { Box, Typography, Container } from '@mui/material';

export class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Container maxWidth="sm">
          <Box sx={{ mt: 5, p: 3, bgcolor: 'error.main', borderRadius: 2 }}>
            <Typography variant="h5" sx={{ color: '#fff', mb: 2 }}>
              ⚠️ Application Error
            </Typography>
            <Typography sx={{ color: '#fff', wordBreak: 'break-word', fontFamily: 'monospace', fontSize: 12 }}>
              {this.state.error?.toString()}
            </Typography>
          </Box>
        </Container>
      );
    }

    return this.props.children;
  }
}
