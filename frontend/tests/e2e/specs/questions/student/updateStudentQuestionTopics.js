describe('Update Student Question Topics walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();
        cy.cleanupStudentQuestions();
        cy.initStudentQuestions({ amount: 1 })
        cy.goToStudentQuestionsAsStudent();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login add and remove topics from student question', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const studentQuestion = data.student_questions[0];

            cy.log('Add topic');
            cy.get('[data-cy="studentQuestionViewTitle"]')
                .parent()
                .parent()
                .filter(`:contains('${studentQuestion.title}')`)
                .find('[data-cy="studentQuestionEditTopics"]')
                .click()
                .get('[data-cy="studentQuestionEditTopicsAdd"]')
                .first()
                .click();

            cy.log('Remove topic');
            cy.get('[data-cy="studentQuestionViewTitle"]')
                .parent()
                .parent()
                .filter(`:contains('${studentQuestion.title}')`)
                .find('[data-cy="studentQuestionEditTopicsRemove"]')
                .first()
                .find('button')
                .click();
        });
    });
    
});
