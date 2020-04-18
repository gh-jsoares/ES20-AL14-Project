describe('List Student Questions as Student walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
    
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      data.student_questions.forEach(studentQuestion => {
        cy.queryDatabase(`INSERT INTO student_questions (id, key, title, content, course_id, student_id, status, creation_date) VALUES ('${studentQuestion.id}', '${studentQuestion.id}', '${studentQuestion.title} ${studentQuestion.id}', '${studentQuestion.content}', '${studentQuestion.course_id}', '${studentQuestion.student_id}', '${studentQuestion.status}', ${studentQuestion.creation_date});`);
      });
    });

    cy.gotoStudentQuestions();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
      data.student_questions.forEach(studentQuestion => {
        cy.queryDatabase(`DELETE FROM student_questions where id = '${studentQuestion.id}';`);
      });
    });
  });

  it('login list student questions', () => {
    cy.contains('Student Question Title')
      .parent()
      .should('have.length', 1)
      .parent()
      .children()
      .should('have.length', 2) // 2 questions
      .children()
      .should('have.length', 14); // 7 * 2 questions
  });
});
