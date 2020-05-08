// commands used for dashboard-related tests

Cypress.Commands.add('goToTournamentsDashboard', () => {
  cy.contains('Dashboard').click();
  cy.get('[data-cy="tournamentsDashTab"]').click();
});

Cypress.Commands.add('assertTournamentStats', () => {
  cy.get('[data-cy="first"]').should('contain', '1');
  cy.get('[data-cy="second"]').should('contain', '0');
  cy.get('[data-cy="third"]').should('contain', '0');
  cy.get('[data-cy="perfect"]').should('contain', '0');
  cy.get('[data-cy="solved"]').should('contain', '1');
  cy.get('[data-cy="unsolved"]').should('contain', '0');
  cy.get('[data-cy="correct"]').should('contain', '2');
  cy.get('[data-cy="wrong"]').should('contain', '2');
  cy.get('[data-cy="score"]').should('contain', '10');
});

Cypress.Commands.add('enterSolvedQuiz', () => {
  cy.get('[data-cy="quiz"]')
    .eq(0)
    .click();
});
