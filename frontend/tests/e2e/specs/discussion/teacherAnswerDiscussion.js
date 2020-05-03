const cleanupFile =
  'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('creating and answering discussion walkthrough', () => {
  before(() => {
    cy.databaseRunFile(cleanupFile);
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.databaseRunFile(cleanupFile);
  });

  it('student logins, creates a discussion, teacher logins and answer discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('Allocation viewtype', 'This is my discussion.');
    cy.contains('Logout').click();
    cy.demoTeacherLogin();
    cy.answerDiscussion('WorkAssignment', 'This is my answer.');
  });
});
