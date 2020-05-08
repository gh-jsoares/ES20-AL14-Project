INSERT INTO users(name,role,username)
    VALUES ('Cypress Test', 'STUDENT','CypressTest');
INSERT INTO users(name,role,username)
    VALUES ('Cypress Test2', 'STUDENT','CypressTest2');
INSERT INTO users_course_executions(users_id, course_executions_id) VALUES((SELECT id FROM users WHERE username='CypressTest'), (SELECT id FROM course_executions WHERE acronym='DemoCourse'));
INSERT INTO users_course_executions(users_id, course_executions_id) VALUES((SELECT id FROM users WHERE username='CypressTest2'), (SELECT id FROM course_executions WHERE acronym='DemoCourse'));
INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date, number_of_questions) VALUES
    ('ONGOING', 'T1-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2019-05-23 10:50','2019-05-23 10:51','2021-05-23 10:52', 1);
INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date, number_of_questions) VALUES
    ('ONGOING', 'T2-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='CypressTest'),
      '2019-05-23 10:50','2021-05-23 10:51','2021-05-23 10:52', 1);
INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date, number_of_questions) VALUES
    ('ENROLL', 'T3-Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2019-05-23 10:50','2021-05-23 10:51','2021-05-23 10:52', 1);
INSERT INTO topics_tournaments VALUES(
    (SELECT id FROM topics WHERE name='Software Architecture'),
    (SELECT id FROM tournaments WHERE title='T1-Cypress Test'));
INSERT INTO topics_tournaments VALUES(
    (SELECT id FROM topics WHERE name='Software Architecture'),
    (SELECT id FROM tournaments WHERE title='T2-Cypress Test'));
INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='T1-Cypress Test'), (SELECT id FROM users WHERE name='Cypress Test'));
INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='T1-Cypress Test'), (SELECT id FROM users WHERE name='Demo Student'));
INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='T2-Cypress Test'), (SELECT id FROM users WHERE name='Cypress Test'));
INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='T2-Cypress Test'), (SELECT id FROM users WHERE name='Cypress Test2'));