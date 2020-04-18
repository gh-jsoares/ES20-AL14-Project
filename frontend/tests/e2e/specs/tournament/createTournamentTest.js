const cleanupFile = 'tests/e2e/support/openTournamentsCleanup.sql';

describe('Student creates tournaments walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.goToTournamentCreation();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  after(() => {
    cy.databaseRunFile(cleanupFile);
  });

  it('create tournament successfully', () => {
    cy.log('try to create tournament with correct input');
    cy.createNewTournament('CypressTest', ['Chrome', 'Uber'], 25, true, 1, 2);

    cy.log('confirm tournament was created');
    cy.closeTournamentAlert('saved', 'New Tournament Saved');
  });

  it('try to create tournament with missing required fields', () => {
    cy.log('try to create tournament with missing required fields');
    cy.createNewTournament('CypressTest', [], 10, true, 1, 2);

    cy.log('confirm request for missing fields');
    cy.closeTournamentAlert('failed', 'Missing required fields');
  });

  it('try to create tournament with start before now', () => {
    cy.log('try to create tournament with start before now');
    cy.createNewTournament('CypressTest', ['Chrome', 'Uber'], 10, false, -1, 1);

    cy.log('confirm request for start after now');
    cy.closeTournamentAlert('failed', 'Start Date must be in the future');
  });

  it('try to create tournament with end before start', () => {
    cy.log('try to create tournament with end before start');
    cy.createNewTournament('CypressTest', ['Chrome', 'Uber'], 10, false, 2, 1);

    cy.log('confirm request for start after now');
    cy.closeTournamentAlert(
      'failed',
      'Conclusion Date must be after Start Date'
    );
  });
});
