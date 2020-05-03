// commands used for tournament-related tests

Cypress.Commands.add('goToOpenTournaments', () => {
  cy.contains('Tournaments').click();
  cy.contains('Open').click();
});

Cypress.Commands.add('searchOpenTournaments', txt => {
  cy.get('[data-cy="searchBar"]')
    .clear()
    .type(txt)
    .type('{enter}');
});

Cypress.Commands.add('assertSearchResults', (data, times) => {
  cy.get('[data-cy="tournRow"]').should($rows => {
    expect($rows).to.have.length(times);
    for (let i = 0; i < times; i++) {
      const cols = $rows.eq(i).children();
      for (let j = 0; j < cols.length - 2; j++) {
        const col = cols.eq(j);
        if (Array.isArray(data[i][j])) {
          expect(col.children()).to.have.length(data[i][j].length);
          for (let k = 0; k < data[i][j].length; k++)
            expect(col).to.contain(data[i][j][k]);
        } else {
          expect(col.text().trim()).to.eq(data[i][j]);
        }
      }
    }
  });
});

Cypress.Commands.add('goToTournamentCreation', () => {
  cy.contains('Tournaments').click();
  cy.contains('Create').click();
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

Cypress.Commands.add('enrollTournament', () => {
  cy.get('[data-cy="enrollBtn"]').click();
});

Cypress.Commands.add('checkTournamentEnroll', hasStarted => {
  cy.get('[data-cy="enrollBtn"]').should('be.disabled')
  if (!hasStarted) cy.get('[data-cy="numEnrolls"]').contains('1');
});

Cypress.Commands.add('cancelTournament', () => {
  cy.get('[data-cy="cancelBtn"]').click();
});

Cypress.Commands.add('checkTournamentDeletion', wasDeleted => {
  if (wasDeleted)
    cy.get('[data-cy="tournTable"]').children().should('have.length',1);
  else
    cy.get('[data-cy="cancelBtn"]').should('not.exist');
});
