INSERT INTO target_sites (site_code, short_name, url)
VALUES ('REMOTEOK', 'RemoteOK API', 'https://remoteok.com/api')
ON CONFLICT (site_code) DO NOTHING;

INSERT INTO target_sites (site_code, short_name, url)
VALUES ('WWR', 'We Work Remotely', 'https://weworkremotely.com')
ON CONFLICT (site_code) DO NOTHING;

INSERT INTO target_sites (site_code, short_name, url)
VALUES ('DJINI', 'Djini Job Board', 'https://djini.co')
ON CONFLICT (site_code) DO NOTHING;