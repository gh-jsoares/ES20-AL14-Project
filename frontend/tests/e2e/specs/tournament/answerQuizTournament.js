//To run these tests first copy cypress.env.json.example rename it to cypress.env.json and change the values inside for db connection
const setupFile = 'tests/e2e/support/tournament/answerQuizTournamentSetup.sql';
const cleanupFile = 'tests/e2e/support/tournament/answerQuizTournamentCleanup.sql';

describe('Student answers a tournament quiz walkthrough', () => {
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

  it('answer tournament quiz successfully and try to answer again', () => {
    cy.searchOpenTournaments('T1-Cypress Test');
    cy.log('try to open the quiz of the tournament');
    cy.startQuiz('SUCCESS');
    cy.log('try to conclude quiz');
    cy.concludeQuiz();
    cy.log('try to open the quiz of the tournament again');
    cy.goToOpenTournaments();
    cy.searchOpenTournaments('T1-Cypress Test');
    cy.startQuiz('FAIL');
  });

  it('try to answer tournament quiz when not enrolled', () => {
    cy.searchOpenTournaments('T2-Cypress Test');
    cy.log('try to answer the quiz');
    cy.startQuiz('FAIL');
  });

  it('try to answer tournament quiz when tournament not started', () => {
    cy.searchOpenTournaments('T3-Cypress Test');
    cy.log('try to answer the quiz');
    cy.startQuiz('ENROLL');
  });

});