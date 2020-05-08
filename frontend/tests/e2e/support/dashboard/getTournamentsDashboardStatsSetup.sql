
INSERT INTO quizzes(available_date, creation_date, title, type, course_execution_id) VALUES ('2019-05-23 10:51','2019-05-23 10:51','Cypress Test', 'TOURNAMENT', (SELECT id FROM course_executions WHERE acronym='DemoCourse'));

INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (0, 1494, (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (3, 1377, (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (2, 1619, (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (1, 1561, (SELECT id FROM quizzes WHERE title='Cypress Test'));

INSERT INTO quiz_answers(answer_date, completed, quiz_id, user_id, used_in_statistics, creation_date) VALUES ('2019-05-24 10:51',true,(SELECT id FROM quizzes WHERE title='Cypress Test'),(SELECT id FROM users WHERE name='Demo Student'),true,'2019-05-24 09:51');


INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=1494 AND correct=true), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=1494 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=1494 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=1377 AND correct=true), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=1377 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=1377 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=1619 AND correct=false LIMIT 1), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=1619 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=1619 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=1561 AND correct=false LIMIT 1), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=1561 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=1561 AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));


INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date, number_of_questions, quiz_id) VALUES
    ('CLOSED', 'Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2019-05-23 10:50','2019-05-23 10:51','2019-06-23 10:51', 4, (SELECT id FROM quizzes WHERE title='Cypress Test'));

INSERT INTO topics_tournaments VALUES(
    (SELECT id FROM topics WHERE name='Software Architecture'),
    (SELECT id FROM tournaments WHERE title='Cypress Test'));

INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='Cypress Test'), (SELECT id FROM users WHERE name='Demo Student'));
