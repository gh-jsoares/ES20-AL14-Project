Cypress.Commands.add('answerQuiz', name => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Available').click();
  cy.contains(name).click();
  cy.contains('End Quiz').click();
  cy.contains('I\'m sure').click();
});

Cypress.Commands.add('createDiscussion', discussion => {
  cy.get('[data-cy="Open Discussion"]').click();
  cy.get('[data-cy="Question Options"]').parent().click();
  cy.get('[data-cy="Your question"]').type(discussion);
  cy.get('[data-cy="sendButton"]').click();
});

Cypress.Commands.add('answerDiscussion', (questionTitle, teacherAnswer) => {
  cy.contains('Management').click();
  cy.contains('Discussions').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="teacherAnswer"]').type(teacherAnswer);
  cy.get('[data-cy="sendButton"]').click();
  cy.get('[data-cy="questionTitleButton"]').should('not.exist');
});

Cypress.Commands.add('seeDiscussion', () => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Discussions').click();
  cy.contains('Discussion Question Title').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="closeButton"]').click();
});
