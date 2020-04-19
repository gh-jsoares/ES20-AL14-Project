describe('View Student Question details as Student walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();

        cy.cleanupStudentQuestions();
        cy.initStudentQuestions({ amount: 1 });

        cy.goToStudentQuestionsAsTeacher();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login accept student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.acceptStudentQuestion(studentQuestion);
        });
    });
});
