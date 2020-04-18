INSERT INTO tournaments(creation_date, available_date, conclusion_date, number_of_questions, state, title, course_execution_id, creator_id) VALUES
	('2020-02-15 11:23', '2030-02-20 16:23', '2030-02-21 16:23',
	10, 'ENROLL', 'CypressTest A1',
	(SELECT id FROM course_executions WHERE acronym='DemoCourse'),
	(SELECT id FROM users WHERE name='Demo Student'));

INSERT INTO tournaments(creation_date, available_date, conclusion_date, number_of_questions, state, title, course_execution_id, creator_id) VALUES
	('2020-02-15 11:23', '2030-02-20 16:23', '2030-02-21 16:23',
	15, 'ENROLL', 'CypressTest A2',
	(SELECT id FROM course_executions WHERE acronym='DemoCourse'),
	(SELECT id FROM users WHERE name='Demo Student'));

INSERT INTO tournaments(creation_date, available_date, conclusion_date, number_of_questions, state, title, course_execution_id, creator_id) VALUES
	('2020-02-15 11:23', '2020-02-20 16:23', '2030-02-21 16:23',
	10, 'ONGOING', 'CypressTest B1',
	(SELECT id FROM course_executions WHERE acronym='DemoCourse'),
	(SELECT id FROM users WHERE name='Demo Student'));

INSERT INTO tournaments(creation_date, available_date, conclusion_date, number_of_questions, state, title, course_execution_id, creator_id) VALUES
	('2020-02-15 11:23', '2030-02-20 16:23', '2030-02-21 16:23',
	25, 'ENROLL', 'CypressTest B2',
	(SELECT id FROM course_executions WHERE acronym='DemoCourse'),
	(SELECT id FROM users WHERE name='Demo Student'));

INSERT INTO tournaments(creation_date, available_date, conclusion_date, number_of_questions, state, title, course_execution_id, creator_id) VALUES
	('2020-02-15 11:23', '2020-02-20 16:23', '2020-02-21 16:23',
	10, 'CLOSED', 'CypressTest C',
	(SELECT id FROM course_executions WHERE acronym='DemoCourse'),
	(SELECT id FROM users WHERE name='Demo Student'));


INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Chrome'),
	(SELECT id FROM tournaments WHERE title='CypressTest A1'));

INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Uber'),
	(SELECT id FROM tournaments WHERE title='CypressTest A2'));

INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Chrome'),
	(SELECT id FROM tournaments WHERE title='CypressTest B1'));

INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Chrome'),
	(SELECT id FROM tournaments WHERE title='CypressTest B2'));

INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Uber'),
	(SELECT id FROM tournaments WHERE title='CypressTest B2'));

INSERT INTO topics_tournaments VALUES(
	(SELECT id FROM topics WHERE name='Chrome'),
	(SELECT id FROM tournaments WHERE title='CypressTest C'));