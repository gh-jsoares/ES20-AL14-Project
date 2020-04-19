Cypress.Commands.add('assertListStudentQuestions', (amount) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .filter(':contains("Student Question Title")')
        .parent()
        .parent()
        .children()
        .should('have.length.of.at.least', amount) // amount questions
        .children()
        .should('have.length.of.at.least', amount * 7); // amount questions * 7 columns
});

Cypress.Commands.add('assertEmptyListStudentQuestions', () => {
    cy.get('[data-cy="student-questions-table"]').then($table => {
        if ($table.find(':contains("No data available")').length != 0) { // has no questions in db
            cy.contains('No data available')
                .parent()
                .should('have.length', 1)
                .parent()
                .children()
                .should('have.length', 1)
                .children()
                .should('have.length', 1);
        } else { // has questions in db, from non tests, real data
            cy.get('[data-cy="studentQuestionViewTitle"]')
                .not(':contains("Student Question Title")')
                .parent()
                .parent()
                .children()
                .should('have.length.of.at.least', 0);
        }
    });
});

Cypress.Commands.add('assertStudentQuestionDetails', (studentQuestion, options) => {
    cy.showStudentQuestionDetails(studentQuestion.title);

    cy.get(`[data-cy="studentQuestionDetailsTitle"]`)
        .contains(`${studentQuestion.title} ${studentQuestion.id}`);

    cy.get(`[data-cy="studentQuestionDetailsStatus"]`)
        .contains(studentQuestion.status);

    cy.get(`[data-cy="studentQuestionDetailsContent"]`)
        .contains(studentQuestion.content);

    if (studentQuestion.status !== 'AWAITING_APPROVAL') {
        cy.get(`[data-cy="studentQuestionDetailsReview"]`)
            .contains(`Last Reviewed by`);
    }

    if (studentQuestion.status === 'REJECTED') {
        cy.get(`[data-cy="studentQuestionDetailsRejected"`)
            .contains(studentQuestion.rejected_explanation);
    }

    options.forEach(option => {
        if (option.correct)
            cy.get(`[data-cy="studentQuestionDetailsOptionCorrect"]`)
                .siblings(`[data-cy="studentQuestionDetailsOption"]`)
                .contains(option.content);
        else
            cy.get(`[data-cy="studentQuestionDetailsOption"]`)
                .contains(option.content);
    });
});
