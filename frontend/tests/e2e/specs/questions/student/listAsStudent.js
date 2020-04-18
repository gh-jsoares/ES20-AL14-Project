describe('List Student Questions as Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    
    cy.studentQuestionsCleanup();
    cy.studentQuestionsInit();

    cy.gotoStudentQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.studentQuestionsCleanup();
  });

  it('login list student questions', () => {
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .children()
        .should('have.length.of.at.least', data.student_questions.length)
        .children()
        .should('have.length.of.at.least', data.student_questions.length * 7);
    });
  });
});
