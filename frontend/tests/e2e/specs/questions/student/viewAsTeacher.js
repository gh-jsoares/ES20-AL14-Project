describe('View Student Question details as Student walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();

        cy.cleanupStudentQuestions();
        cy.initStudentQuestions({ amount: 1, offset: 3 });

        cy.goToStudentQuestionsAsTeacher();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login view student question details', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[3];
            cy.assertStudentQuestionDetails(studentQuestion, data.options);
        });
    });
});
