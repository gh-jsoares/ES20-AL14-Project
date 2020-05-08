const cleanupFile =
  'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('creating, answering, and sending a new question about the discussion', () => {
  before(() => {
    cy.databaseRunFile(cleanupFile);
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.databaseRunFile(cleanupFile);
  });

  it('student logins, creates a discussion, teacher logins, answer discussion, student logins and makes new question', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('Allocation viewtype', 'This is my discussion.');
    cy.contains('Logout').click();
    cy.demoTeacherLogin();
    cy.answerDiscussion('WorkAssignment', 'This is my answer.');
    cy.contains('Logout').click();
    cy.demoStudentLogin();
    cy.makeNewQuestion('This is my new question about this discussion');
  });
});
