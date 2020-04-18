DELETE FROM topics_tournaments WHERE tournaments_id IN
    (SELECT id FROM tournaments WHERE title LIKE 'CypressTest%');

DELETE FROM tournaments WHERE title LIKE 'CypressTest%';