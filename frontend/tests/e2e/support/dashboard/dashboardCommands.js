// commands used for dashboard-related tests

Cypress.Commands.add('goToTournamentsDashboard', () => {
  cy.contains('Dashboard').click();
  cy.get('[data-cy="tournamentsDashboardBtn"]').click();
});
