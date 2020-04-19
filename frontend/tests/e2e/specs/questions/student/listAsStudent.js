describe('List Student Questions as Student walkthrough', () => {
    beforeEach(() => {
        cy.demoStudentLogin();
        cy.cleanupStudentQuestions();
    });

    afterEach(() => {
        cy.contains('Logout').click();
        cy.cleanupStudentQuestions();
    });

    it('login list student questions', () => {
        cy.initStudentQuestions();
        cy.goToStudentQuestions();

        cy.fixture('questions/student/studentQuestionsData.json').then(data => {
            cy.assertListStudentQuestions(data.student_questions.length);
        });
    });

    it('login list shows only submitted student questions', () => {
        cy.initStudentQuestions({ student_id: 678, amount: 2 }); // other student
        cy.initStudentQuestions({ student_id: 676, amount: 2, offset: 2,  course_id: 3 }); // other course
        cy.initStudentQuestions({ amount: 2, offset: 4 }); // demo student

        cy.goToStudentQuestions();
        cy.assertListStudentQuestions(2);
    });
    
    it('login list has no student questions', () => {
        cy.goToStudentQuestions();
        cy.assertEmptyListStudentQuestions();
    });
});
