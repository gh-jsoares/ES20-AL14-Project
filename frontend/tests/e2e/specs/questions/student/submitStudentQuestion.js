describe('Submit Student Question walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    cy.studentQuestionsCleanup();
    cy.gotoStudentQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.studentQuestionsCleanup();
  });

  it('login create student question', () => {
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      const studentQuestion = data.student_questions[0]
      cy.createStudentQuestion(studentQuestion, data.options)
    });
  });
});
