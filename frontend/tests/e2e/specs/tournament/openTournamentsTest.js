const setupFile = 'tests/e2e/support/openTournamentsSetup.sql';
const cleanupFile = 'tests/e2e/support/openTournamentsCleanup.sql';

describe('search/view open tournaments walkthrough', () => {
  before(() => {
    // creates open tournaments with title CypressTest A1, A2, B1, B2
    // and a closed tournament with title CypressTest C
    cy.databaseRunFile(cleanupFile);
    cy.databaseRunFile(setupFile);
  });

  beforeEach(() => {
    cy.demoStudentLogin();
    cy.goToOpenTournaments();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  after(() => {
    cy.databaseRunFile(cleanupFile);
  });

  it('check if only open tournaments appear', () => {
    cy.log('search matching all tournaments created for the test');
    cy.searchOpenTournaments('CypressTest');

    cy.log('expect to see only the open tournaments created');
    cy.fixture('openTournamentData.json').then(data => {
      cy.assertSearchResults([data.B2, data.B1, data.A2, data.A1], 4);
    });
  });

  it('search with partial title hides some tournaments', () => {
    cy.log('search matching type A tournaments');
    cy.searchOpenTournaments('CypressTest A');

    cy.log('expect to see only the 2 type A tournaments');
    cy.fixture('openTournamentData.json').then(data => {
      cy.assertSearchResults([data.A2, data.A1], 2);
    });
  });

  it('search with full title shows right tournament', () => {
    cy.log('search matching only A1');
    cy.searchOpenTournaments('CypressTest A1');

    cy.log('expect to see only A1');
    cy.fixture('openTournamentData.json').then(data => {
      cy.assertSearchResults([data.A1], 1);
    });
  });

  it('search with non-matching title shows no tournaments', () => {
    cy.log('search not matching any tournament');
    cy.searchOpenTournaments('CypressTest F');

    cy.log('expect to find no tournaments');
    cy.assertSearchResults([], 0);
  });
});
