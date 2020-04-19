const cleanupFile =
  'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('creating Discussion walkthrough', () => {
  before(() => {
    cy.databaseRunFile(cleanupFile);
  });

  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.databaseRunFile(cleanupFile);
  });

  it('student logins and creates a discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('This is my discussion.');
  });

  it('student tries to create duplicate discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('This is my discussion.');
    cy.createDiscussion('This is the duplicate discussion.');

    cy.closeErrorMessage();
    cy.log('close dialog');
    cy.get('[data-cy="cancelButton"]').click();
  });

  it('student tries to create empty discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('   ');

    cy.closeErrorMessage();
    cy.log('close dialog');
    cy.get('[data-cy="cancelButton"]').click();
  });
});
