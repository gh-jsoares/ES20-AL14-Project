const cleanupFile =
    'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('creating, answering, opening discussion and seeing discussion stats walkthrough', () => {
    before(() => {
        cy.databaseRunFile(cleanupFile);
        cy.demoStudentLogin();
    });

    afterEach(() => {
        cy.databaseRunFile(cleanupFile);
    });

    it('student logins, creates a discussion, teacher logins, answer discussion, make it public, student logins ' +
        'and sees his discussion stats', () => {
        cy.answerQuiz('Allocation viewtype');
        cy.createDiscussion('Allocation viewtype', 'This is my discussion.');
        cy.contains('Logout').click();
        cy.demoTeacherLogin();
        cy.answerDiscussion('WorkAssignment', 'This is my answer.');
        cy.openDiscussion('WorkAssignment');
        cy.contains('Logout').click();
        cy.demoStudentLogin();
        cy.seeDiscussionStats();
    });
});
