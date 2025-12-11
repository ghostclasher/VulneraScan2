import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import styles from './Footer.module.css';

const Footer = () => {
  const year = new Date().getFullYear();
  const version = import.meta.env.VITE_APP_VERSION || 'v1.0.0';

  return (
    <footer className={styles.footer} role="contentinfo">
      <div className={styles.brand} style={{ textAlign: 'center' }}>
        <div style={{ fontWeight: 800, fontSize: 16 }}>VulneraScan</div>
        <div style={{ fontSize: 12, opacity: 0.9 }}>Security scanning platform • {version}</div>
      </div>

      <nav aria-label="footer-links" style={{ display: 'flex', gap: 14, alignItems: 'center', flexWrap: 'wrap' }}>
        <RouterLink to="/about">About</RouterLink>
        <RouterLink to="/docs">Docs</RouterLink>
        <RouterLink to="/profile">My Account</RouterLink>
        <a href="/swagger-ui/index.html" target="_blank" rel="noopener noreferrer">API</a>
        <RouterLink to="/privacy">Privacy</RouterLink>
        <RouterLink to="/terms">Terms</RouterLink>
        <a href="mailto:support@vulnerascan.example">Contact</a>
      </nav>

      <div className={styles.legal} style={{ textAlign: 'center' }}>
        <div>© {year} VulneraScan. All rights reserved.</div>
        <div style={{ marginTop: 4, fontSize: 12, opacity: 0.9 }}>Made with security in mind.</div>
      </div>
    </footer>
  );
};

export default Footer;