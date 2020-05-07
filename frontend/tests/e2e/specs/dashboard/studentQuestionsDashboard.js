describe('Student question Dashboard walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();

        cy.cleanupStudentQuestions();
        cy.initStudentQuestions();
        cy.get('[data-cy="dashboardButton"]').click();
        cy.get('[data-cy="studentQuestionDashboardLink"]').click();
    });

    afterEach(() => {
        cy.cleanupStudentQuestions();
    });

    it('login view student question dashboard', () => {
        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            const approved = data.student_questions.filter(sq => sq.status == 'ACCEPTED').length;
            const rejected = data.student_questions.filter(sq => sq.status == 'REJECTED').length;
            const total = data.student_questions.length;
            const percentage = Math.round(approved / total * 100);

            cy.get('[data-cy="approved"]')
                .should('have.text', `${approved}`)
                .get('[data-cy="rejected"]')
                .should('have.text', `${rejected}`)
                .get('[data-cy="total"]')
                .should('have.text', `${total}`)
                .get('[data-cy="percentage"]')
                .should('have.text', `${percentage}%`);
        });
    });
});
