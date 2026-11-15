
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router';
import App from './App.tsx';
import { AuthProvider } from './contexto/AuthProvider.tsx';


import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.min.css';
import './Nier-tema.css';


createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <BrowserRouter>
            {/* Toda la aplicación puede leer la sesión con useAuth() */}
            <AuthProvider>
                <App />
            </AuthProvider>
        </BrowserRouter>
    </StrictMode>,
);

