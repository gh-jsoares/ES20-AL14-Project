const dbUser = Cypress.env('db_username');
const dbPassword = Cypress.env('db_password');
const dbName = Cypress.env('db_name');

const dbAccess =
  Cypress.platform === 'win32'
    ? `SET PGPASSWORD=${dbPassword} && psql -U ${dbUser} -d ${dbName}`
    : `PGPASSWORD=${dbPassword} psql -U ${dbUser} -d ${dbName}`;
Cypress.Commands.add('databaseRunFile', filename => {
  cy.exec(`${dbAccess} -f ${filename}`);
});

Cypress.Commands.add('queryDatabase', query => {
  cy.exec(`${dbAccess} -c "${query}"`);
});
