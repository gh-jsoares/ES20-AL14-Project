describe('creating Discussion walkthrough', () => {
  beforeEach(() => {
    cy.demoStudentLogin();
  });

  afterEach(() => {
    cy.contains('Logout').click();
    cy.deleteDiscussion('Allocation viewtype', 'This is my discussion.');
  });

  it('student logins and creates a discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('This is my discussion.');
  });

  it('student tries to create duplicate discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('This is my discussion.');
    cy.createDiscussion('This is the duplicate discussion.');

    cy.closeErrorMessage();
    cy.log('close dialog');
    cy.get('[data-cy="cancelButton"]').click();
  });

  it('student tries to create empty discussion', () => {
    cy.answerQuiz('Allocation viewtype');
    cy.createDiscussion('   ');

    cy.closeErrorMessage();
    cy.log('close dialog');
    cy.get('[data-cy="cancelButton"]').click();
  });
});
