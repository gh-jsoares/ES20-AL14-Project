describe('List Student Questions as Student walkthrough', () => {
    beforeEach(() => {
      cy.demoStudentLogin();
      cy.queryDatabase(`INSERT INTO student_questions (id, key, title, content, course_id, student_id, status, creation_date) VALUES ('10000','10000', 'Student Question Title 1', 'Student Question Content', '2', '676', 'AWAITING_APPROVAL', NOW());`);
      cy.wait(500);
      cy.gotoStudentQuestions();
    });
  
    afterEach(() => {
      cy.contains('Logout').click();
      cy.queryDatabase(`DELETE FROM student_questions where id = '10000';`);
    });
  
    it('login view student question details', () => {
      cy.contains('Student Question Title')
        .parent()
        .children()
        .find('[data-cy="viewStudentQuestionDetails"]')
        .click();
      cy.wait(2000);
    });
});
  