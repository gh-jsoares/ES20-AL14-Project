//To run these tests first copy cypress.env.json.example rename it to cypress.env.json and change the values inside for db connection
const setupFile =
  'tests/e2e/support/dashboard/getTournamentsDashboardStatsSetup.sql';
const cleanupFile =
  'tests/e2e/support/dashboard/getTournamentsDashboardCleanup.sql';

describe('Student views tournaments dashboard info walkthrough', () => {
  beforeEach(() => {
    cy.log('try to clean datatabase before test');
    cy.databaseRunFile(cleanupFile);
    cy.log('try to setup database before test');
    cy.databaseRunFile(setupFile);
    cy.demoStudentLogin();
    cy.goToTournamentsDashboard();
  });

  afterEach(() => {
    cy.get('[data-cy="logoutButton"]').click();
  });

  after(() => {
    cy.log('try to clean database');
    cy.databaseRunFile(cleanupFile);
  });

  it('view tournament dashboard stats and open closed/answered tournament quiz', () => {
    cy.assertTournamentStats();
    cy.enterSolvedQuiz();
  });
});
