Cypress.Commands.add('goToStudentQuestions', () => {
    cy.server()
        .route('/courses/2/topics').as('getTopics')
        .route('/courses/2/questions/student').as('getStudentQuestions');

    cy.contains('Student Questions')
        .click()
        .wait(['@getTopics', '@getStudentQuestions']);
});

Cypress.Commands.add('assertListStudentQuestions', (amount) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .filter(':contains("Student Question Title")')
        .parent()
        .parent()
        .children()
        .should('have.length.of.at.least', amount) // amount questions
        .children()
        .should('have.length.of.at.least', amount * 7); // amount questions * 7 columns
});

Cypress.Commands.add('assertEmptyListStudentQuestions', () => {
    cy.get('[data-cy="student-questions-table"]').then($table => {
        if ($table.find(':contains("No data available")').length != 0) { // has no questions in db
            cy.contains('No data available')
                .parent()
                .should('have.length', 1)
                .parent()
                .children()
                .should('have.length', 1)
                .children()
                .should('have.length', 1);
        } else { // has questions in db, from non tests, real data
            cy.get('[data-cy="studentQuestionViewTitle"]')
                .not(':contains("Student Question Title")')
                .parent()
                .parent()
                .children()
                .should('have.length.of.at.least', 0);
        }
    });
});

Cypress.Commands.add('createStudentQuestion', (studentQuestion, options) => {
    cy.get('[data-cy="studentQuestionNew"]')
        .click()
        .get('[data-cy="studentQuestionNewTitle"]')
        .type(`${studentQuestion.title} ${studentQuestion.id}`)
        .get('[data-cy="studentQuestionNewContent"]')
        .type(studentQuestion.content);

    options.forEach((option, i) => {
        cy.get(`[data-cy="studentQuestionNewOption-${i + 1}-content"]`)
            .type(option.content);
        if (option.correct) {
            cy.get(`[data-cy="studentQuestionNewOption-${i + 1}-correct"]`)
                .parent()
                .parent()
                .click();
        }
    });
    
    cy.get('[data-cy="studentQuestionNewSave"]')
        .click();
})

Cypress.Commands.add('showStudentQuestionDetails', (title) => {
    cy.get('[data-cy="studentQuestionViewTitle"]')
        .parent()
        .parent()
        .filter(`:contains('${title}')`)
        .children()
        .find('[data-cy="viewStudentQuestionDetails"]')
        .click();
});

Cypress.Commands.add('assertStudentQuestionDetails', (studentQuestion, options) => {
    cy.showStudentQuestionDetails(studentQuestion.title);

    cy.get(`[data-cy="studentQuestionDetailsTitle"]`)
        .contains(`${studentQuestion.title} ${studentQuestion.id}`);

    cy.get(`[data-cy="studentQuestionDetailsStatus"]`)
        .contains(studentQuestion.status);

    cy.get(`[data-cy="studentQuestionDetailsContent"]`)
        .contains(studentQuestion.content);

    if (studentQuestion.status !== 'AWAITING_APPROVAL') {
        cy.get(`[data-cy="studentQuestionDetailsReview"]`)
            .contains(`Last Reviewed by`);
    }

    if (studentQuestion.status === 'REJECTED') {
        cy.get(`[data-cy="studentQuestionDetailsRejected"`)
            .contains(studentQuestion.rejected_explanation);
    }

    options.forEach((option, i) => {
        if (option.correct)
            cy.get(`[data-cy="studentQuestionDetailsOptionCorrect"]`)
                .siblings(`[data-cy="studentQuestionDetailsOption"]`)
                .contains(option.content);
        else
            cy.get(`[data-cy="studentQuestionDetailsOption"]`)
                .contains(option.content)
    })
});

Cypress.Commands.add('initStudentQuestions', ({ amount, student_id, course_id, offset = 0 } = {}) => {
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {

        for (let i = offset; i < data.student_questions.length; i++) {
            if (amount && i == amount + offset) break;

            const studentQuestion = data.student_questions[i];

            if (student_id)
                studentQuestion.student_id = student_id;

            if (course_id)
                studentQuestion.course_id = course_id;

            let status_keys = '';
            let status_vals = '';

            if (studentQuestion.status !== 'AWAITING_APPROVAL') {
                status_keys = 'reviewed_date, last_reviewer_id,';
                status_vals = `${studentQuestion.reviewed_date}, '${studentQuestion.last_reviewer_id}',`;
            }

            if (studentQuestion.status === 'REJECTED') {
                status_keys += 'rejected_explanation,';
                status_vals += `'${studentQuestion.rejected_explanation}',`;
            }

            cy.queryDatabase(
                `INSERT INTO student_questions\
                (\
                    id,\
                    key,\
                    title,\
                    content,\
                    course_id,\
                    student_id,\
                    status,\
                    ${status_keys}\
                    creation_date\
                )\
                VALUES\
                (\
                    '${studentQuestion.id}',\
                    '${studentQuestion.id}',\
                    '${studentQuestion.title} ${studentQuestion.id}',\
                    '${studentQuestion.content}',\
                    '${studentQuestion.course_id}',\
                    '${studentQuestion.student_id}',\
                    '${studentQuestion.status}',\
                     ${status_vals}\
                     ${studentQuestion.creation_date}\
                );`
            );
            data.options.forEach(option => {
                cy.queryDatabase(
                    `INSERT INTO options\
                    (content, correct, student_question_id)\
                    VALUES\
                    (\
                        '${option.content}',\
                        '${option.correct}',\
                        '${studentQuestion.id}'\
                    );`
                );
            });
        }
    });
});

Cypress.Commands.add('cleanupStudentQuestions', () => {
    cy.fixture('questions/student/studentQuestionsData.json').then(data => {
        data.student_questions.forEach(studentQuestion => {
            cy.queryDatabase(
                `WITH sq_id AS (\
                SELECT id FROM student_questions WHERE title LIKE '${studentQuestion.title}%'\
                ) DELETE FROM options WHERE student_question_id IN (SELECT id FROM sq_id);\
            `);
            cy.queryDatabase(`DELETE FROM student_questions WHERE title LIKE '${studentQuestion.title}%';`);
        });
    });
});
