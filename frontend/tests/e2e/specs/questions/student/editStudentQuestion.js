describe('Submit Student Question walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();
        cy.cleanupStudentQuestion(10003);

        cy.initStudentQuestions({ amount: 1, offset: 3 });

        cy.goToStudentQuestionsAsStudent();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestion(10003);
    });

    it('login edit student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[3];
            cy.editStudentQuestion(studentQuestion);
            cy.checkStudentQuestionStatus(studentQuestion, 'AWAITING_APPROVAL');
        });
    });

});
