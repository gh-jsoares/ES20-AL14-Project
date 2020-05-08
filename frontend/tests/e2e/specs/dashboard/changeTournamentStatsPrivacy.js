//To run these tests first copy cypress.env.json.example rename it to cypress.env.json and change the values inside for db connection

describe('Student changes privacy of tournament stats walkthrough', () => {

  beforeEach(() => {
    cy.demoStudentLogin();
    cy.goToTournamentsDashboard();
  });

  afterEach(() => {
    cy.contains('Logout').click();
  });

  it('make tournament stats information private and then make it public', () => {
    cy.log('try to make tournament stats private');
    cy.contains('Make Private').parent().click();
    cy.contains('Make Public');
    cy.log('logout and log back in');
    cy.contains('Logout').click();
    cy.demoStudentLogin();
    cy.log('got to tournaments dashboard and check if its still private');
    cy.goToTournamentsDashboard();
    cy.contains('Make Public');
    cy.log('try to make tournament stats public');
    cy.contains('Make Public').parent().click();
    cy.contains('Make Private');
  });

});