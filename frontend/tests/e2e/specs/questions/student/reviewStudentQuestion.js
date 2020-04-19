describe('Review Student Question walkthrough', () => {
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

    it('login reject student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.server(); // cannot be chained
            cy.route('put', `/questions/student/all/${studentQuestion.id}/reject`).as('rejectStudentQuestion');

            cy.rejectStudentQuestion(studentQuestion, data.explanation)
                .wait('@rejectStudentQuestion')
                .checkStudentQuestionStatus(studentQuestion, 'REJECTED');
        });
    });

    it('login reject student question with empty explanation', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];
            cy.server(); // cannot be chained
            cy.route('put', `/questions/student/all/${studentQuestion.id}/reject`).as('rejectStudentQuestion');

            cy.log('try to reject without explanation');
            cy.rejectStudentQuestion(studentQuestion, ' ')

            cy.closeErrorMessage();

            cy.log('close dialog');
            cy.get('[data-cy="cancelButton"]').click();
        });
    });
});
