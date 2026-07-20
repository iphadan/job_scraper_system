import React, { useState, useEffect } from 'react';
import './Home.css';

const Home = ({ onLogout }) => {
    const [requests, setRequests] = useState([]);
    const [supportedBoards, setSupportedBoards] = useState([]);
    
    // 🌟 New State for Scraped Jobs Feed
    const [jobs, setJobs] = useState([]);
    const [activeTab, setActiveTab] = useState('strategies'); // 'strategies' or 'jobs'
    const [isLoadingJobs, setIsLoadingJobs] = useState(false);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [keywords, setKeywords] = useState('');
    const [selectedSites, setSelectedSites] = useState({});
    
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const username = localStorage.getItem('username');

    // 1. Fetch user requests & supported sites from database
    const fetchDashboardData = async () => {
        const token = localStorage.getItem('token');
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
            // Fetch user strategies
            const reqResponse = await fetch('/api/requests', { headers });
            if (reqResponse.status === 403 || reqResponse.status === 401) {
                throw new Error('Unauthorized access or session expired.');
            }
            const reqData = await reqResponse.json();
            setRequests(reqData);

            // Fetch target site directory dynamically from database
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

    // 🌟 2. Fetch all scraped jobs from API
    const fetchScrapedJobs = async () => {
        setIsLoadingJobs(true);
        const token = localStorage.getItem('token');
        const headers = { 'Authorization': `Bearer ${token}` };

        try {
            const response = await fetch('/api/jobs', { headers });
            if (response.ok) {
                const jobsData = await response.json();
                setJobs(jobsData);
            } else {
                throw new Error('Failed to load scraped job listings.');
            }
        } catch (err) {
            setError(err.message);
        } finally {
            setIsLoadingJobs(false);
        }
    };

    useEffect(() => {
        fetchDashboardData();
        fetchScrapedJobs();
    }, []);

    // Toggle tab view and refresh jobs if clicking "Scraped Jobs"
    const handleSwitchTab = (tab) => {
        setActiveTab(tab);
        if (tab === 'jobs') {
            fetchScrapedJobs();
        }
    };

    const handleCheckboxChange = (code) => {
        setSelectedSites(prev => ({ ...prev, [code]: !prev[code] }));
    };

    const handleCloseModal = () => {
        setIsModalOpen(false);
        setKeywords('');
        setSelectedSites(supportedBoards.reduce((acc, b) => ({ ...acc, [b.siteCode]: false }), {}));
        setError('');
    };

    const handleCreateStrategy = async (e) => {
        e.preventDefault();
        if (!keywords.trim()) return;

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
            {/* Header Area */}
            <div className="home-header">
                <h1>Welcome back, <span className="username-accent">{username}</span></h1>
                
                <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                    {/* Mode Toggle Button */}
                    <button 
                        onClick={() => handleSwitchTab(activeTab === 'strategies' ? 'jobs' : 'strategies')} 
                        className="scraped-jobs-btn"
                        style={{ background: activeTab === 'jobs' ? '#10b981' : '#4f46e5', color: '#fff' }}
                    >
                        {activeTab === 'strategies' ? '🕵️‍♂️ View Scraped Jobs' : '⚙️ View Strategies'}
                    </button>

                    <button onClick={onLogout} className="logout-btn">Logout</button>
                </div>
            </div>
            <hr />

            {/* Error Banner */}
            {error && <div className="error-banner">{error}</div>}

            {/* View Switcher Controls */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                <h2>
                    {activeTab === 'strategies' ? 'Active Scraping Strategies' : `Scraped Job Feed (${jobs.length})`}
                </h2>

                {activeTab === 'strategies' ? (
                    <button onClick={() => setIsModalOpen(true)} className="open-modal-btn">
                        ➕ Add Scraper Strategy
                    </button>
                ) : (
                    <button onClick={fetchScrapedJobs} className="open-modal-btn" style={{ background: '#3b82f6' }}>
                        🔄 Refresh Feed
                    </button>
                )}
            </div>

            {/* TAB 1: STRATEGIES GRID */}
            {activeTab === 'strategies' && (
                requests.length === 0 ? (
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
                )
            )}

            {/* TAB 2: SCRAPED JOBS FEED */}
            {activeTab === 'jobs' && (
                isLoadingJobs ? (
                    <p style={{ color: '#666', fontStyle: 'italic' }}>Fetching latest scraped jobs...</p>
                ) : jobs.length === 0 ? (
                    <p style={{ color: '#666', fontStyle: 'italic' }}>No job listings extracted yet. Wait for the background worker to execute!</p>
                ) : (
                    <div className="strategy-grid">
                        {jobs.map(job => (
                            <div key={job.id} className="card" style={{ borderLeft: '4px solid #10b981' }}>
                                <h3>{job.title}</h3>
                                <p style={{ color: '#4b5563', fontWeight: 'bold', margin: '0.5rem 0' }}>
                                    🏢 {job.company}
                                </p>
                                <p style={{ color: '#6b7280', fontSize: '0.9rem', marginBottom: '1rem', maxHeight: '100px', overflow: 'hidden' }}>
                                    {job.description || 'No detailed description provided.'}
                                </p>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem' }}>
                                    <a 
                                        href={job.url} 
                                        target="_blank" 
                                        rel="noopener noreferrer" 
                                        style={{ color: '#2563eb', fontWeight: 'bold', textDecoration: 'none' }}
                                    >
                                        Apply Now ↗
                                    </a>
                                    <small style={{ color: '#9ca3af' }}>
                                        {job.scrapedAt ? new Date(job.scrapedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : 'Recently'}
                                    </small>
                                </div>
                            </div>
                        ))}
                    </div>
                )
            )}

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
        </div>
    );
};

export default Home;