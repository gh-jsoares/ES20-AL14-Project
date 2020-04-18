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
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })
/// <reference types="Cypress" />
Cypress.Commands.add('demoAdminLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="adminButton"]').click();
  cy.contains('Administration').click();
  cy.contains('Manage Courses').click();
});

Cypress.Commands.add('createCourseExecution', (name, acronym, academicTerm) => {
  cy.get('[data-cy="createButton"]').click();
  cy.get('[data-cy="Name"]').type(name);
  cy.get('[data-cy="Acronym"]').type(acronym);
  cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
  cy.get('[data-cy="saveButton"]').click();
});

Cypress.Commands.add('closeErrorMessage', (name, acronym, academicTerm) => {
  cy.contains('Error')
    .parent()
    .find('button')
    .click();
});

Cypress.Commands.add('deleteCourseExecution', acronym => {
  cy.contains(acronym)
    .parent()
    .should('have.length', 1)
    .children()
    .should('have.length', 7)
    .find('[data-cy="deleteCourse"]')
    .click();
});

Cypress.Commands.add(
  'createFromCourseExecution',
  (name, acronym, academicTerm) => {
    cy.contains(name)
      .parent()
      .should('have.length', 1)
      .children()
      .should('have.length', 7)
      .find('[data-cy="createFromCourse"]')
      .click();
    cy.get('[data-cy="Acronym"]').type(acronym);
    cy.get('[data-cy="AcademicTerm"]').type(academicTerm);
    cy.get('[data-cy="saveButton"]').click();
  }
);

// New

const dbUser = '';
const dbPassword = '';
const dbName = 'tutordb';

const dbAccess =
  'PGPASSWORD=' +
  dbPassword +
  ' psql -d ' +
  dbName +
  ' -U ' +
  dbUser +
  ' -h localhost';

Cypress.Commands.add('databaseRunFile', filename => {
  cy.exec(dbAccess + ' -f ' + filename);
});

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
});

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
