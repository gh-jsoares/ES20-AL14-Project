describe('Review Student Question walkthrough', () => {
    beforeEach(() => {
        cy.demoTeacherLogin();

        cy.cleanupStudentQuestion(10000);
        cy.initStudentQuestions({ amount: 1 });

        cy.goToStudentQuestionsAsTeacher();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestion(10000);
    });

    it('login edit topics and accept student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.acceptStudentQuestion(studentQuestion);
            cy.log('Add topic');
            cy.get('[data-cy="studentQuestionEditTopics"]')
                .click()
                .get('[data-cy="studentQuestionEditTopicsAdd"]')
                .first()
                .click();

            cy.log('Remove topic');
            cy.get('[data-cy="studentQuestionEditTopicsRemove"]')
                .find('button')
                .click();
            cy.get('[data-cy="stepperContinueContent"]')
                .click()
                .get('[data-cy="stepperContinueReview"]')
                .click()
                .get('[data-cy="stepperApprove"]')
                .click()
                .wait('@acceptStudentQuestion')
                .checkStudentQuestionStatus(studentQuestion, 'ACCEPTED');
        });
    });

    it('login edit content and accept student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.acceptStudentQuestion(studentQuestion);
            cy.get('[data-cy="stepperContinueContent"]')
                .click()
                .get('[data-cy="studentQuestionNewTitle"]')
                .scrollIntoView()
                .clear()
                .type(`Update ${studentQuestion.title} ${studentQuestion.id}`)
                .get('[data-cy="studentQuestionNewContent"]')
                .clear()
                .type('Update ' + studentQuestion.content);
            cy.get('[data-cy="stepperContinueReview"]')
                .click()
                .get('[data-cy="stepperApprove"]')
                .click()
                .wait('@acceptStudentQuestion')
                .checkStudentQuestionApproved(studentQuestion);
            console.log(studentQuestion.title);
        });
    });
});
