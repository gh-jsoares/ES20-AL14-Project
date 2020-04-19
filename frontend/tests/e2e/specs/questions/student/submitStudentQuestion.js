describe('Submit Student Question walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();
        cy.cleanupStudentQuestions();
        cy.goToStudentQuestionsAsStudent();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login create student question and view details', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.createStudentQuestion(studentQuestion, data.options);
            cy.assertStudentQuestionDetails(studentQuestion, data.options);
        });
    });

    it('login create question duplicate', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.createStudentQuestion(studentQuestion, data.options);

            cy.log('try to create with the same title');
            cy.createStudentQuestion(studentQuestion, data.options);

            cy.closeErrorMessage();

            cy.log('close dialog');
            cy.get('[data-cy="cancelButton"]').click();
        });
    });
});
