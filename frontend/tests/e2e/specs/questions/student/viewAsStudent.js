describe('List Student Questions as Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      const studentQuestion = data.student_questions[0];
      cy.queryDatabase(`INSERT INTO student_questions (id, key, title, content, course_id, student_id, status, creation_date) VALUES ('${studentQuestion.id}', '${studentQuestion.id}', '${studentQuestion.title} ${studentQuestion.id}', '${studentQuestion.content}', '${studentQuestion.course_id}', '${studentQuestion.student_id}', '${studentQuestion.status}', ${studentQuestion.creation_date});`);
    });

    cy.gotoStudentQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();

    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      const studentQuestion = data.student_questions[0];
      cy.queryDatabase(`DELETE FROM student_questions where id = '${studentQuestion.id}';`);
    });
  });

  it('login view student question details', () => {
    cy.contains('Student Question Title')
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 7)
      .find('[data-cy="viewStudentQuestionDetails"]')
      .click();
  });
});
  