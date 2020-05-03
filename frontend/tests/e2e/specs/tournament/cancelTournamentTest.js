//To run these tests first copy cypress.env.json.example rename it to cypress.env.json and change the values inside for db connection
const setupFile = 'tests/e2e/support/tournament/cancelTournamentSetup.sql';
const cleanupFile = 'tests/e2e/support/tournament/cancelTournamentCleanup.sql';

describe('Student cancels tournaments walkthrough', () => {
  beforeEach(() => {
    cy.log('try to clean datatabase before test');
    cy.databaseRunFile(cleanupFile);
    cy.log('try to setup database before test');
    cy.databaseRunFile(setupFile);
    cy.demoStudentLogin();
    cy.goToOpenTournaments();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  after(() => {
    cy.log('try to clean database');
    cy.databaseRunFile(cleanupFile);
  });

  it('cancel tournament successfully', () => {
    cy.searchOpenTournaments('T1-Cypress Test');
    cy.log('try to cancel open tournament');
    cy.cancelTournament();
    cy.log('confirm cancel was successfull');
    cy.checkTournamentDeletion(true);
  });

  it('try to cancel tournament when tournament has started', () => {
    cy.searchOpenTournaments('T2-Cypress Test');
    cy.log('check if cancellation is possible');
    cy.checkTournamentDeletion(false);
  });

  it('try to cancel tournament when user is not the creator', () => {
    cy.searchOpenTournaments('T3-Cypress Test');
    cy.log('check if cancellation is possible');
    cy.checkTournamentDeletion(false);
  });
});