// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This is will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })


Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
  cy.contains('Tournaments').click();
});

Cypress.Commands.add('goToTournamentCreation', () => {
  cy.contains('Tournaments').click();
  cy.contains('Create').click();
});

Cypress.Commands.add('goToOpenTournaments', () => {
  cy.contains('Tournaments').click();
  cy.contains('Available').click();
});

Cypress.Commands.add(
  'createNewTournament',
  (title, topics, questions, scramble, start, end) => {
    if (!!title) cy.get('[data-cy="Title"]').type(title);
    for (let t of topics)
      cy.get('[data-cy="Topics"]').type(
        t + '{enter}' + '{backspace}'.repeat(t.length)
      );
    cy.get('[data-cy="Topics"]').type('{esc}');
    if (!!questions) {
      cy.get('[data-cy="QuestText"]').type('{backspace}' + questions);
    }
    if (!!scramble)
      cy.get('[data-cy="Scramble"]')
        .parent()
        .click();

    if (!!start) {
      cy.contains('*Start Date')
        .parent()
        .click();
      cy.selectTournamentDate(start);
    }
    if (!!end) {
      cy.contains('*Conclusion Date')
        .parent()
        .click();
      cy.selectTournamentDate(end);
    }

    cy.get('[data-cy="createBtn"]').click();
  }
);

Cypress.Commands.add('selectTournamentDate', date => {
  cy.get('.v-dialog--active').within(() => {
    if (date === 0) cy.get('.v-date-picker-table__current').click();
    else {
      let n = Math.abs(date);
      let btnClass = date > 0 ? '.mdi-chevron-right' : '.mdi-chevron-left';
      for (let i = 0; i < n; i++) cy.get(btnClass).click();

      cy.wait(500);
      cy.get('.v-date-picker-table')
        .contains('1')
        .click();
    }

    cy.get('.v-card__actions')
      .contains('OK')
      .click();
  });
});

Cypress.Commands.add('closeTournamentAlert', (type, msg) => {
  cy.get('[data-cy="' + type + '"]')
    .should('be.visible')
    .should('contain.text', msg)
    .find('button')
    .click();
});
