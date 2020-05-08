
INSERT INTO quizzes(available_date, creation_date, title, type, course_execution_id) VALUES ('2019-05-23 10:51','2019-05-23 10:51','Cypress Test', 'TOURNAMENT', (SELECT id FROM course_executions WHERE acronym='DemoCourse'));

INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (0, (SELECT id FROM questions WHERE title='Allowing the incorporation of independently developed components'), (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (3, (SELECT id FROM questions WHERE title='Restrict vocabulary'), (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (2, (SELECT id FROM questions WHERE title='The impact of functionalities on the architecture'), (SELECT id FROM quizzes WHERE title='Cypress Test'));
INSERT INTO quiz_questions(sequence, question_id, quiz_id) VALUES (1, (SELECT id FROM questions WHERE title='Impact of the business goals on the architecture'), (SELECT id FROM quizzes WHERE title='Cypress Test'));

INSERT INTO quiz_answers(answer_date, completed, quiz_id, user_id, used_in_statistics, creation_date) VALUES ('2019-05-24 10:51',true,(SELECT id FROM quizzes WHERE title='Cypress Test'),(SELECT id FROM users WHERE name='Demo Student'),true,'2019-05-24 09:51');


INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=(SELECT id FROM questions WHERE  title='Allowing the incorporation of independently developed components') AND correct=true), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE title='Allowing the incorporation of independently developed components') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE title='Allowing the incorporation of independently developed components') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=(SELECT id FROM questions WHERE  title='Restrict vocabulary') AND correct=true), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='Restrict vocabulary') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='Restrict vocabulary') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=(SELECT id FROM questions WHERE  title='The impact of functionalities on the architecture') AND correct=false LIMIT 1), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='The impact of functionalities on the architecture') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='The impact of functionalities on the architecture') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));

INSERT INTO question_answers(time_taken, option_id, quiz_answer_id, quiz_question_id, sequence) VALUES (10000, (select id from options where question_id=(SELECT id FROM questions WHERE  title='Impact of the business goals on the architecture') AND correct=false LIMIT 1), (SELECT id from quiz_answers WHERE creation_date='2019-05-24 09:51'),
    (SELECT id FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='Impact of the business goals on the architecture') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')),
    (SELECT sequence FROM quiz_questions WHERE question_id=(SELECT id FROM questions WHERE  title='Impact of the business goals on the architecture') AND quiz_id IN (SELECT id FROM quizzes WHERE title='Cypress Test')));


INSERT INTO tournaments(state, title, course_execution_id, creator_id, creation_date, available_date, conclusion_date, number_of_questions, quiz_id) VALUES
    ('CLOSED', 'Cypress Test', (SELECT id FROM course_executions WHERE acronym='DemoCourse'),(SELECT id FROM users WHERE username='Demo-Student'),
      '2019-05-23 10:50','2019-05-23 10:51','2019-06-23 10:51', 4, (SELECT id FROM quizzes WHERE title='Cypress Test'));

INSERT INTO topics_tournaments VALUES(
    (SELECT id FROM topics WHERE name='Software Architecture'),
    (SELECT id FROM tournaments WHERE title='Cypress Test'));

INSERT INTO tournaments_enrolled_students(enrolled_tournaments_id, enrolled_students_id) VALUES ((SELECT id FROM tournaments WHERE title='Cypress Test'), (SELECT id FROM users WHERE name='Demo Student'));
