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
    cy.contains('Private').parent().click();
    cy.contains('Public');
    cy.log('logout and log back in');
    cy.contains('Logout').click();
    cy.demoStudentLogin();
    cy.log('got to tournaments dashboard and check if its still private');
    cy.goToTournamentsDashboard();
    cy.contains('Public');
    cy.log('try to make tournament stats public');
    cy.contains('Public').parent().click();
    cy.contains('Private');
  });

});