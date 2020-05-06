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
      for (let j = 0; j < cols.length - 1; j++) {
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
      cy.get('[data-cy="startDate"]').click();
      cy.selectTournamentDate('startDate', start);
    }
    if (!!end) {
      cy.get('[data-cy="endDate"]').click();
      cy.selectTournamentDate('endDate', end);
    }

    cy.get('[data-cy="createBtn"]').click();
  }
);

Cypress.Commands.add('selectTournamentDate', (place, date) => {
  cy.get(`[data-cy="${place}"]`)
    .parent()
    .parent()
    .find('.datetimepicker')
    .within((datePicker) => {
      let n = Math.abs(date);
      let arrow = date > 0 ? '{rightarrow}' : '{leftarrow}';
      for (let i = 0; i < n; i++) {
        let btn =
          i === 0 ? '.datepicker-today' : '.datepicker-day-keyboard-selected';
        cy.get(btn)
          .parent()
          .type(arrow);
      }
      let btn =
        date === 0 ? '.datepicker-today' : '.datepicker-day-keyboard-selected';
      cy.get(btn)
        .parent()
        .click();
      cy.get('button.validate').click();
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
  if (!hasStarted) {
    cy.get('[data-cy="enrollBtn"]').should('be.disabled');
    cy.get('[data-cy="numEnrolls"]').contains('1');
  } else {
    cy.get('[data-cy="enrollBtn"]').should('not.exist');
  }
});

Cypress.Commands.add( 'startQuiz',  type => {
  if (type === 'SUCCESS')
    cy.get('[data-cy="solveQuizBtn"]').click();
  else if (type == 'FAIL')
    cy.get('[data-cy="solveQuizBtn"]').should('be.disabled');
  else
    cy.get('[data-cy="solveQuizBtn"]').should('not.exist');
});

Cypress.Commands.add('concludeQuiz', () => {
  cy.get('[data-cy="endQuizBtn"]').click();
  cy.get('[data-cy="concludeQuizBtn"]').click()
});
