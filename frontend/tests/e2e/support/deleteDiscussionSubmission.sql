DELETE FROM discussions WHERE message_from_student = 'This is my discussion.';

DELETE FROM question_answers qa WHERE EXISTS (SELECT * FROM quiz_answers a, quizzes q WHERE qa.quiz_answer_id = a.id AND a.quiz_id = q.id AND q.title = 'Allocation viewtype');

DELETE FROM quiz_answers a WHERE EXISTS (SELECT * FROM quizzes q WHERE a.quiz_id = q.id AND q.title = 'Allocation viewtype');
