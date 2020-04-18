describe('View Student Question details as Student walkthrough', () => {
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
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      const studentQuestion = data.student_questions[0]
      cy.get('[data-cy="studentQuestionViewTitle"]')
      .parent()
      .parent()
      .filter(`:contains('${studentQuestion.title}')`)
      .children()
      .find('[data-cy="viewStudentQuestionDetails"]')
      .click();
    });
  });
});
  