const cleanupFile =
  'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('altering privacy switch in student discussion stats', () => {
  before(() => {
    cy.databaseRunFile(cleanupFile);
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.databaseRunFile(cleanupFile);
  });

  it('student logins and changes privacy setting in discussion stats', () => {
    cy.clickInDiscussionPrivacySwitch();
    cy.contains('Logout').click();
    cy.demoStudentLogin();
    cy.clickInDiscussionPrivacySwitch();
    cy.contains('Logout').click();
  });
});
