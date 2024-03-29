//To run these tests first copy cypress.env.json.example rename it to cypress.env.json and change the values inside for db connection
const setupFile = 'tests/e2e/support/tournament/enrollTournamentSetup.sql';
const cleanupFile = 'tests/e2e/support/tournament/enrollTournamentCleanup.sql';

describe('Student enrolls in tournaments walkthrough', () => {
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

  it('enroll in tournament successfully', () => {
    cy.searchOpenTournaments('T1-Cypress Test');
    cy.log('try to enroll in open tournament');
    cy.enrollTournament();
    cy.log('confirm enroll was successfull');
    cy.checkTournamentEnroll(false);
  });

  it('try to enroll in tournament when tournament has started', () => {
    cy.searchOpenTournaments('T2-Cypress Test');
    cy.log('check if enroll is possible');
    cy.checkTournamentEnroll(true);
  });
});