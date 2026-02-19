import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import MobileApp from './MobileApp.jsx'

const isMobile = /Mobi|Android|iPhone|iPad|iPod/i.test(navigator.userAgent) || window.innerWidth < 768;
const userConfig = window.BUDDI_CONFIG || {};

createRoot(document.getElementById('root')).render(
  <StrictMode>
    {isMobile ? <MobileApp userConfig={userConfig} /> : <App />}
  </StrictMode>,
)
