DELETE FROM question_answers WHERE quiz_answer_id IN (SELECT id FROM quiz_answers WHERE quiz_id IN (SELECT quiz_id FROM tournaments WHERE title LIKE 'Cypress Test%'));
DELETE FROM quiz_answers WHERE quiz_id IN (SELECT quiz_id FROM tournaments WHERE title LIKE 'Cypress Test%');
DELETE FROM quiz_questions WHERE quiz_id IN (SELECT quiz_id FROM tournaments WHERE title LIKE 'Cypress Test%');
DELETE FROM topics_tournaments WHERE tournaments_id IN (SELECT id FROM tournaments WHERE title LIKE 'Cypress Test%');
DELETE FROM tournaments_enrolled_students WHERE enrolled_tournaments_id in
  (SELECT id FROM tournaments WHERE title LIKE 'Cypress Test%');
DELETE FROM tournaments WHERE title LIKE 'Cypress Test%';
DELETE FROM quizzes WHERE type='TOURNAMENT';