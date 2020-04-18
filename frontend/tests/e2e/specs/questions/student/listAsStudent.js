describe('List Student Questions as Student walkthrough', () => {
    beforeEach(() => {
      cy.demoStudentLogin();
      cy.queryDatabase(`INSERT INTO student_questions (id, key, title, content, course_id, student_id, status, creation_date) VALUES ('10000','10000', 'Student Question Title 1', 'Student Question Content', '2', '676', 'AWAITING_APPROVAL', NOW());`);
      cy.queryDatabase(`INSERT INTO student_questions (id, key, title, content, course_id, student_id, status, creation_date) VALUES ('20000','20000', 'Student Question Title 2', 'Student Question Content', '2', '676', 'AWAITING_APPROVAL', NOW());`);
      cy.wait(500);
      cy.gotoStudentQuestions();
    });
  
    afterEach(() => {
      cy.contains('Logout').click();
      cy.queryDatabase(`DELETE FROM student_questions where id = '10000';`);
      cy.queryDatabase(`DELETE FROM student_questions where id = '20000';`);
    });
  
    it('login list student questions', () => {
      cy.contains('Student Question Title')
        .parent()
        .should('have.length', 1)
        .parent()
        .children()
        .should('have.length', 2) // 2 questions
        .children()
        .should('have.length', 14) // 7 * 2 questions
    });
});
  