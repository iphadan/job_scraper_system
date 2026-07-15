import React, { useState, useEffect } from 'react';
import './Home.css';

const Home = ({ onLogout }) => {
    const [requests, setRequests] = useState([]);
    
    // 🌟 State holding the DB-sourced supported boards
    const [supportedBoards, setSupportedBoards] = useState([]);
    
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [keywords, setKeywords] = useState('');
    
    // Checkbox mapping state (dynamically populated)
    const [selectedSites, setSelectedSites] = useState({});
    
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const username = localStorage.getItem('username');

    // Fetch user requests & supported sites from database
    const fetchDashboardData = async () => {
        const token = localStorage.getItem('token');
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
            // 1. Fetch user strategies
            const reqResponse = await fetch('/api/requests', { headers });
            if (reqResponse.status === 403 || reqResponse.status === 401) {
                throw new Error('Unauthorized access session expired.');
            }
            const reqData = await reqResponse.json();
            setRequests(reqData);

            // 🌟 2. Fetch target site directory dynamically from database
            const sitesResponse = await fetch('/api/target-sites', { headers });
            if (sitesResponse.ok) {
                const sitesData = await sitesResponse.json();
                setSupportedBoards(sitesData);
                
                // Initialize selection state using DB keys
                setSelectedSites(
                    sitesData.reduce((acc, site) => ({ ...acc, [site.siteCode]: false }), {})
                );
            }
        } catch (err) {
            setError(err.message);
        }
    };

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const handleCheckboxChange = (code) => {
        setSelectedSites(prev => ({ ...prev, [code]: !prev[code] }));
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setKeywords('');
        // Reset check states based on DB boards
        setSelectedSites(supportedBoards.reduce((acc, b) => ({ ...acc, [b.siteCode]: false }), {}));
        setError('');
    };

    const handleCreateStrategy = async (e) => {
        e.preventDefault();
        if (!keywords.trim()) return;

        // Map selections
        const targetSitesPayload = Object.keys(selectedSites)
            .filter(code => selectedSites[code])
            .map(code => ({ siteCode: code }));

        if (targetSitesPayload.length === 0) {
            setError('Please select at least one target platform.');
            return;
        }

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
                body: JSON.stringify({ 
                    keywords: keywords, 
                    targetSites: targetSitesPayload 
                }) 
            });

            if (!response.ok) throw new Error('Failed to save tracking strategy.');

            handleCloseModal();
            await fetchDashboardData();
        } catch (err) {
            setError(err.message);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="home-container">
            <div className="home-header">
                <h1>Welcome back, <span className="username-accent">{username}</span></h1>
                <button onClick={onLogout} className="logout-btn">Logout</button>
            </div>
            <hr />

            <button onClick={() => setIsModalOpen(true)} className="open-modal-btn">
                ➕ Add Scraper Strategy
            </button>

            {error && <div className="error-banner">{error}</div>}

            {/* Modal Layer */}
            {isModalOpen && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h2>New Scraping Strategy</h2>
                        <hr />
                        
                        <form onSubmit={handleCreateStrategy}>
                            <div className="form-group">
                                <label htmlFor="keywords">Target Keywords</label>
                                <input 
                                    id="keywords"
                                    type="text" 
                                    placeholder="e.g., Python, Remote React, Docker"
                                    value={keywords}
                                    onChange={(e) => setKeywords(e.target.value)}
                                    className="text-input"
                                    required
                                    disabled={isSubmitting}
                                />
                            </div>

                            <div className="form-group">
                                <label>Select Target Job Boards</label>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '0.5rem' }}>
                                    {/* 🌟 Dynamically render checkboxes from DB query */}
                                    {supportedBoards.length === 0 ? (
                                        <p style={{ color: '#888', fontStyle: 'italic' }}>Loading job boards...</p>
                                    ) : (
                                        supportedBoards.map(board => (
                                            <label key={board.siteCode} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                                                <input 
                                                    type="checkbox" 
                                                    checked={!!selectedSites[board.siteCode]} 
                                                    onChange={() => handleCheckboxChange(board.siteCode)}
                                                    disabled={isSubmitting}
                                                /> 
                                                {board.shortName}
                                            </label>
                                        ))
                                    )}
                                </div>
                            </div>

                            <div className="modal-actions">
                                <button type="button" onClick={handleCloseModal} className="cancel-btn" disabled={isSubmitting}>Cancel</button>
                                <button type="submit" className="submit-btn" disabled={isSubmitting}>
                                    {isSubmitting ? 'Saving...' : 'Create Scraper'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <h3>Active Scraping Strategies</h3>
            {requests.length === 0 ? (
                <p style={{ color: '#666', fontStyle: 'italic' }}>No tracking strategies configured yet.</p>
            ) : (
                <div className="strategy-grid">
                    {requests.map(req => (
                        <div key={req.id} className="card">
                            <h4>🔑 Keywords: {req.keywords}</h4>
                            <p style={{ margin: '0.75rem 0', color: '#555' }}>
                                🌐 Target Engines: {req.targetSites && req.targetSites.length > 0 ? (
                                    req.targetSites.map((site, idx) => (
                                        <span key={idx} style={{ marginRight: '0.4rem' }}>
                                            <code style={{ background: '#e8f0fe', color: '#1a73e8', padding: '0.2rem 0.4rem', borderRadius: '4px', fontWeight: 'bold' }} title={site.url}>
                                                {site.shortName}
                                            </code>
                                        </span>
                                    ))
                                ) : (
                                    <span style={{ color: '#888', fontStyle: 'italic' }}>None mapped</span>
                                )}
                            </p>
                            <p>Status: <span className="status-badge">{req.status}</span></p>
                            <small style={{ color: '#888' }}>ID Reference: #{req.id}</small>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Home;