import React, { createContext, useState, useContext } from 'react';
import { createTheme, ThemeProvider } from '@mui/material/styles';

const ThemeContext = createContext();

export function useTheme() {
  return useContext(ThemeContext);
}

export function ThemeContextProvider({ children }) {
  const [isDark, setIsDark] = useState(() => {
    try {
      const saved = localStorage.getItem('theme-mode');
      return saved ? saved === 'dark' : true;
    } catch {
      return true;
    }
  });

  const theme = createTheme({
    palette: {
      mode: isDark ? 'dark' : 'light',
      primary: { main: '#00d4ff' },
      secondary: { main: '#ff006e' },
      background: {
        default: isDark ? '#071029' : '#f7fbff',
        paper: isDark ? '#0f1730' : '#ffffff',
      },
      text: {
        primary: isDark ? '#e6f7ff' : '#0f1724',
        secondary: isDark ? 'rgba(230,247,255,0.75)' : 'rgba(15,23,36,0.7)',
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Segoe UI", sans-serif',
      h6: { fontWeight: 700 },
    },
    shape: { borderRadius: 12 },
    components: {
      MuiButton: {
        styleOverrides: {
          root: { textTransform: 'none' },
        },
      },
    },
  });

  const toggleTheme = () => {
    setIsDark((prev) => {
      const next = !prev;
      try { localStorage.setItem('theme-mode', next ? 'dark' : 'light'); } catch {}
      return next;
    });
  };

  return (
    <ThemeContext.Provider value={{ isDark, toggleTheme }}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </ThemeContext.Provider>
  );
}