Cypress.Commands.add('goToStudentQuestionsAsTeacher', () => {
    cy.server(); // cannot be chained

    cy.route('/courses/2/questions/student/all').as('getStudentQuestions');

    cy.contains('Student Questions')
        .click()
        .wait('@getStudentQuestions');
});

Cypress.Commands.add('acceptStudentQuestion', studentQuestion => {
    cy.server(); // cannot be chained

    cy.route('put', `/questions/student/all/${studentQuestion.id}/approve`).as('acceptStudentQuestion');

    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('${studentQuestion.title}')`)
        .children()
        .find('[data-cy="approveStudentQuestion"]')
        .click();
});

Cypress.Commands.add('checkStudentQuestionApproved', (studentQuestion) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('Update ${studentQuestion.title} ${studentQuestion.id}')`)
        .contains('ACCEPTED');
});

Cypress.Commands.add('checkStudentQuestionStatus', (studentQuestion, status) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('${studentQuestion.title}')`)
        .contains(status);
});

Cypress.Commands.add('rejectStudentQuestion', (studentQuestion, explanation) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('${studentQuestion.title}')`)
        .children()
        .find('[data-cy="rejectStudentQuestionDialog"]')
        .click()
        .get('[data-cy="studentQuestionRejectExplanation"]')
        .type(explanation)
        .get('[data-cy="rejectStudentQuestion"]')
        .click();
});
