import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Home.css'; // Reuse existing styles or import JobList.css

export const JobList = () => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const fetchJobs = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    try {
      const response = await fetch('/api/jobs', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch scraped jobs');
      }

      const data = await response.json();
      setJobs(data);
    } catch (err) {
      console.error("Failed to load jobs:", err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
    // Auto-refresh feed every 30 seconds
    const interval = setInterval(fetchJobs, 30000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="home-container">
      {/* Navigation Header */}
      <div className="home-header">
        <h1>🕵️‍♂️ Scraped Job Feed</h1>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button onClick={() => navigate('/')} className="open-modal-btn">
            ⚙️ Manage Strategies
          </button>
          <button onClick={fetchJobs} className="scraped-jobs-btn" style={{ background: '#3b82f6' }}>
            🔄 Refresh
          </button>
        </div>
      </div>
      <hr />

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <p style={{ color: '#666', fontStyle: 'italic' }}>Loading scraped listings...</p>
      ) : jobs.length === 0 ? (
        <div style={{ textAlign: 'center', margin: '2rem 0' }}>
          <p style={{ color: '#666', fontStyle: 'italic' }}>
            No jobs found yet. Make sure your background worker is active and strategies are configured!
          </p>
        </div>
      ) : (
        <div className="strategy-grid">
          {jobs.map((job) => (
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
      )}
    </div>
  );
};

export default JobList;