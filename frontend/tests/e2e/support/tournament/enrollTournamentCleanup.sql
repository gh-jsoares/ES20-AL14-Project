DELETE FROM tournaments_enrolled_students WHERE enrolled_tournaments_id in
  (SELECT id FROM tournaments WHERE title LIKE '%Cypress Test');
DELETE FROM tournaments WHERE title LIKE '%Cypress Test%';