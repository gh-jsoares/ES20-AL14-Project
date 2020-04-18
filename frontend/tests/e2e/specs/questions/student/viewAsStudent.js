describe('List Student Questions as Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    
    cy.studentQuestionsCleanup();
    cy.studentQuestionsInit(1);

    cy.gotoStudentQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.studentQuestionsCleanup();
  });

  it('login view student question details', () => {
    cy.contains('Student Question Title')
      .parent()
      .children()
      .find('[data-cy="viewStudentQuestionDetails"]')
      .click();
  });
});
  