import React, { useState } from 'react';

const Login = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        setIsLoading(true);

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password }),
            });

            if (!response.ok) {
                throw new Error('Invalid Active Directory credentials. Access Denied.');
            }

            const data = await response.json();
            
            // Store token securely in browser storage
            localStorage.setItem('token', data.accessToken);
            localStorage.setItem('username', username);
            
            // Notify parent app of successful login state change
            onLoginSuccess();
            
        } catch (error) {
            setErrorMessage(error.message || 'Connection failed to the API Gateway.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <form onSubmit={handleSubmit} style={styles.form}>
                <h2>Enterprise Job Scraper Login</h2>
                
                {errorMessage && <div style={styles.error}>{errorMessage}</div>}
                
                <div style={styles.inputGroup}>
                    <label>Username</label>
                    <input 
                        type="text" 
                        value={username} 
                        onChange={(e) => setUsername(e.target.value)} 
                        required 
                        disabled={isLoading}
                    />
                </div>
                
                <div style={styles.inputGroup}>
                    <label>Password</label>
                    <input 
                        type="password" 
                        value={password} 
                        onChange={(e) => setPassword(e.target.value)} 
                        required 
                        disabled={isLoading}
                    />
                </div>
                
                <button type="submit" style={styles.button} disabled={isLoading}>
                    {isLoading ? 'Verifying Credentials...' : 'Login'}
                </button>
            </form>
        </div>
    );
};

const styles = {
    container: { display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#f5f5f5' },
    form: { padding: '2rem', background: 'white', borderRadius: '8px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)', width: '300px' },
    inputGroup: { marginBottom: '1rem', display: 'flex', flexDirection: 'column' },
    button: { width: '100%', padding: '0.75rem', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' },
    error: { color: 'red', backgroundColor: '#fce8e6', padding: '0.5rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.9rem' }
};

export default Login;