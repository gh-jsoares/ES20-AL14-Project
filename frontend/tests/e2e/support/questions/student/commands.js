/* Shared Commands */
Cypress.Commands.add('showStudentQuestionDetails', (title) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('${title}')`)
        .children()
        .find('[data-cy="viewStudentQuestionDetails"]')
        .click();
});

/* Import other Commands */
import './dbCommands';
import './assertCommands';
import './studentCommands';
import './teacherCommands';
