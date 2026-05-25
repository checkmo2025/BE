UPDATE report
SET redirect_url = CONCAT('/profile/', SUBSTRING(redirect_url, LENGTH('/api/members/') + 1))
WHERE report_target_type = 'MEMBER'
  AND redirect_url LIKE '/api/members/%';