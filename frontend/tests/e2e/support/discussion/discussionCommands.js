Cypress.Commands.add('answerQuiz', name => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Available').click();
  cy.contains(name).click();
  cy.contains('End Quiz').click();
  cy.contains('I\'m sure').click();
});

Cypress.Commands.add('createDiscussion', (name, discussion) => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Solved').click();
  cy.contains(name).click();
  cy.get('[data-cy="Open Discussion"]').click();
  cy.get('[data-cy="Question Options"]')
    .parent()
    .click();
  cy.get('[data-cy="Your question"]').type(discussion);
  cy.get('[data-cy="sendButton"]').click();
});

Cypress.Commands.add('answerDiscussion', (questionTitle, teacherAnswer) => {
  cy.contains('Management').click();
  cy.contains('Discussions').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="teacherAnswer"]').type(teacherAnswer);
  cy.get('[data-cy="sendButton"]').click();
  cy.get('[data-cy="notAnswered"]').should('not.exist');
});

Cypress.Commands.add('seeDiscussion', () => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Discussions').click();
  cy.contains('Discussion Question Title').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="closeButton"]').click();
});

Cypress.Commands.add('openDiscussion', () => {
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="openDiscussionButton"]').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="isVisibleToOtherStudents"]').should('exist');
});

Cypress.Commands.add('makeNewQuestion', newQuestion => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Discussions').click();
  cy.contains('Discussion Question Title').click();
  cy.get('[data-cy="questionTitleButton"]').click();
  cy.get('[data-cy="Your question"]').type(newQuestion);
  cy.get('[data-cy="sendButton"]').click();
});

Cypress.Commands.add('seeQuestionDiscussions', quizTitle => {
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Solved').click();
  cy.contains(quizTitle).click();
  cy.get('[data-cy="getDiscussionsButton"]').click();
  cy.get('[data-cy="questionDiscussions"]').should('exist');
  cy.get('[data-cy="visibleDiscussion"]').click();
});

Cypress.Commands.add('seeDiscussionStats', () => {
  cy.get('[data-cy="dashboardButton"]').click();
  cy.contains('Discussions').click();
  cy.get('[data-cy="discussionsNumber"]').should('exist');
  cy.get('[data-cy="publicDiscussionsNumber"]').should('exist');
});
