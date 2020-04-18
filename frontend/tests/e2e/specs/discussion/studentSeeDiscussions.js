describe('Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin()
    })

    afterEach(() => {
        cy.contains('Logout').click()
    })

    it('login see discussion', () => {
        // cy.createDiscussion(...)

        cy.seeDiscussion('SOAQualities');
    });

});