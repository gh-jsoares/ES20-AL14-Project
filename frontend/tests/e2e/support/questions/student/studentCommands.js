Cypress.Commands.add('goToStudentQuestionsAsStudent', () => {
    cy.server(); // cannot be chained

    cy.route('/courses/2/topics').as('getTopics')
        .route('/courses/2/questions/student').as('getStudentQuestions');

    cy.contains('Student Questions')
        .click()
        .wait(['@getTopics', '@getStudentQuestions']);
});

Cypress.Commands.add('createStudentQuestion', (studentQuestion, options) => {
    
    cy.server(); // cannot be chained

    cy.route('post', '/courses/2/questions/student').as('saveStudentQuestion');

    cy.get('[data-cy="studentQuestionNew"]')
        .click()
        .get('[data-cy="studentQuestionNewTitle"]')
        .type(`${studentQuestion.title} ${studentQuestion.id}`)
        .get('[data-cy="studentQuestionNewContent"]')
        .type(studentQuestion.content);

    options.forEach((option, i) => {
        cy.get(`[data-cy="studentQuestionNewOption-${i + 1}-content"]`)
            .type(option.content);
        if (option.correct) {
            cy.get(`[data-cy="studentQuestionNewOption-${i + 1}-correct"]`)
                .parent()
                .parent()
                .click();
        }
    });
    
    cy.get('[data-cy="studentQuestionNewSave"]')
        .click()
        .wait('@saveStudentQuestion');
})
