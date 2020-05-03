DELETE FROM tournaments WHERE title LIKE '%Cypress Test%';
DELETE FROM users_course_executions WHERE users_id IN (SELECT id FROM users WHERE username='CypressTest');
DELETE FROM users WHERE username='CypressTest';
