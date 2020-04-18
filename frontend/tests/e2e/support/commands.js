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

Cypress.Commands.add('demoStudentLogin', () => {
  cy.visit('/');
  cy.get('[data-cy="studentButton"]').click();
  cy.get('[data-cy="quizzesButton"]').click();
  cy.contains('Discussions').click();
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

Cypress.Commands.add('answerDiscussion', (questionTitle, teacherAnswer) => {
    cy.contains(questionTitle)
        .parent()
        .click({force: true})
    cy.get('[data-cy="teacherAnswer"]').type(teacherAnswer)
    cy.get('[data-cy="sendButton"]').click()
    cy.contains(questionTitle).should('not.exist')
})

Cypress.Commands.add('seeDiscussion', (questionTitle) => {
    cy.contains('Discussions').click();
    cy.contains(questionTitle)
        .parent()
        .click({force: true})
    cy.get('[data-cy="closeButton"]').click()
})

const dbUser = 'hello';
const dbPassword = 'yaqXIT123';
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