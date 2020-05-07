const cleanupFile =
    'tests/e2e/support/discussion/deleteDiscussionSubmission.sql';

describe('creating, answering and opening discussion walkthrough', () => {
    before(() => {
        cy.databaseRunFile(cleanupFile);
        cy.demoStudentLogin();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.databaseRunFile(cleanupFile);
    });

    it('student logins, creates a discussion, teacher logins, answer discussion and make it public', () => {
        cy.answerQuiz('Allocation viewtype');
        cy.createDiscussion('Allocation viewtype', 'This is my discussion.');
        cy.contains('Logout').click();
        cy.demoTeacherLogin();
        cy.answerDiscussion('WorkAssignment', 'This is my answer.');
        cy.openDiscussion('WorkAssignment');
        cy.contains('Logout').click();
        cy.demoStudentLogin();
        cy.seeQuestionDiscussions('Allocation viewtype');
    });
});
