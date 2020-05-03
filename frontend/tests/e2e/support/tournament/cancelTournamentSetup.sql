INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date) VALUES
    ('ENROLL', 'T1-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2020-05-23 10:50','2020-05-23 10:51','2020-05-23 10:52');
INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date) VALUES
    ('ONGOING', 'T2-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2020-05-23 10:50','2020-05-23 10:51','2020-05-23 10:52');
INSERT INTO users(creation_date,name,role,username)
    VALUES ('2019-05-23 10:50','Cypress Test', 'STUDENT','CypressTest');
INSERT INTO users_course_executions(users_id, course_executions_id) VALUES((SELECT id FROM users WHERE username='CypressTest'), (SELECT id FROM course_executions WHERE acronym='DemoCourse'));
INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date) VALUES
    ('ENROLL', 'T3-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='CypressTest'),
      '2020-05-23 10:50','2020-05-23 10:51','2020-05-23 10:52');