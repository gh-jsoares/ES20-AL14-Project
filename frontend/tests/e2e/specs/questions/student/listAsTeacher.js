describe('List Student Questions as Teacher walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();
        cy.cleanupStudentQuestions();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login list student questions', () => {
        cy.initStudentQuestions();
        cy.goToStudentQuestionsAsTeacher();

        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            cy.assertListStudentQuestions(data.student_questions.length);
        });
    });

    it('login lists all student questions in course', () => {
        cy.initStudentQuestions({ student_id: 655, amount: 2 }); // other student
        cy.initStudentQuestions({ student_id: 676, amount: 2, offset: 2,  course_id: 3 }); // other course
        cy.initStudentQuestions({ amount: 2, offset: 4 }); // demo student

        cy.goToStudentQuestionsAsTeacher();
        cy.assertListStudentQuestions(4);
    });
    
    it('login list has no student questions', () => {
        cy.goToStudentQuestionsAsTeacher();
        cy.assertEmptyListStudentQuestions();
    });
});
