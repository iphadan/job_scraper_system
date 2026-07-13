import React, { useState, useEffect } from 'react';

const Home = ({ onLogout }) => {
    const [requests, setRequests] = useState([]);
    const [keywords, setKeywords] = useState('');
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const username = localStorage.getItem('username');

    // Reusable fetch function to load tracking strategies
    const fetchScraperRequests = async () => {
        const token = localStorage.getItem('token');
        try {
            const response = await fetch('/api/requests', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.status === 403 || response.status === 401) {
                throw new Error('Unauthorized access session expired.');
            }

            if (response.ok) {
                const data = await response.json();
                setRequests(data);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    useEffect(() => {
        fetchScraperRequests();
    }, []);

    // Handle Form Submission (POST Request)
    const handleCreateStrategy = async (e) => {
        e.preventDefault();
        if (!keywords.trim()) return;

        setError('');
        setIsSubmitting(true);
        const token = localStorage.getItem('token');

        try {
            const response = await fetch('/api/requests', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                // Notice we DO NOT send a username field here! The backend resolves it securely via JWT.
                body: JSON.stringify({ keywords }) 
            });

            if (!response.ok) {
                throw new Error('Failed to create scraping strategy.');
            }

            // Reset form field and refresh the dashboard list view
            setKeywords('');
            await fetchScraperRequests();

        } catch (err) {
            setError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div style={{ padding: '2rem', fontFamily: 'Arial, sans-serif' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h1>Welcome back, <span style={{ color: '#007bff' }}>{username}</span></h1>
                <button onClick={onLogout} style={styles.logoutBtn}>Logout</button>
            </div>
            <hr />

            {/* Strategy Creation Section */}
            <div style={styles.formContainer}>
                <h3>Create New Scraper Strategy</h3>
                <form onSubmit={handleCreateStrategy} style={{ display: 'flex', gap: '1rem' }}>
                    <input 
                        type="text" 
                        placeholder="e.g., Python Developer, Remote React, Docker Engineer"
                        value={keywords}
                        onChange={(e) => setKeywords(e.target.value)}
                        disabled={isSubmitting}
                        style={styles.input}
                        required
                    />
                    <button type="submit" disabled={isSubmitting} style={styles.submitBtn}>
                        {isSubmitting ? 'Saving...' : 'Add Strategy'}
                    </button>
                </form>
            </div>

            {error && <p style={{ color: 'red', backgroundColor: '#fce8e6', padding: '0.5rem', borderRadius: '4px' }}>{error}</p>}

            {/* Dashboard Workspace List Display */}
            <h3>Your Scraping Strategies</h3>
            {requests.length === 0 ? (
                <p style={{ color: '#666', fontStyle: 'italic' }}>No active subscription tracking strategies found. Use the form above to add one!</p>
            ) : (
                <div style={styles.grid}>
                    {requests.map(req => (
                        <div key={req.id} style={styles.card}>
                            <h4>🔑 Keywords: {req.keywords}</h4>
                            <p>Status: <span style={styles.statusBadge}>{req.status}</span></p>
                            <small style={{ color: '#888' }}>ID Reference: #{req.id}</small>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

const styles = {
    logoutBtn: { padding: '0.5rem 1rem', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer' },
    formContainer: { backgroundColor: '#f8f9fa', padding: '1.5rem', borderRadius: '8px', marginBottom: '2rem', border: '1px solid #e9ecef' },
    input: { flex: 1, padding: '0.75rem', borderRadius: '4px', border: '1px solid #ced4da', fontSize: '1rem' },
    submitBtn: { padding: '0.75rem 1.5rem', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' },
    grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1.5rem', marginTop: '1rem' },
    card: { border: '1px solid #dee2e6', padding: '1rem', borderRadius: '8px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' },
    statusBadge: { backgroundColor: '#e2f0d9', color: '#385723', padding: '0.2rem 0.5rem', borderRadius: '4px', fontSize: '0.85rem', fontWeight: 'bold' }
};

export default Home;