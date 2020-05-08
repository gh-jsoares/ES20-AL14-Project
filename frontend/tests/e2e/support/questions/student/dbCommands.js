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

            const options = data.options.map(option => {
                return `INSERT INTO options\
                    (content, correct, student_question_id)\
                    VALUES\
                    (\
                        '${option.content}',\
                        '${option.correct}',\
                        '${studentQuestion.id}'\
                    )`
            }).join('; ');

            cy.queryDatabase(
                `START TRANSACTION; INSERT INTO student_questions\
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
                ); ${options}; COMMIT;`
            );
        }
    });
});

Cypress.Commands.add('cleanupStudentQuestion', (id) => {
    cy.queryDatabase(
        `DELETE FROM options WHERE student_question_id = '${id}';\
        DELETE FROM student_questions WHERE id = '${id}';\
    `);
});


Cypress.Commands.add('cleanupStudentQuestions', () => {
    cy.queryDatabase(
        `WITH sq_id AS (\
            SELECT id FROM student_questions WHERE title LIKE 'Student Question Title%'\
        ) DELETE FROM options WHERE student_question_id IN (SELECT id FROM sq_id);\
        DELETE FROM student_questions WHERE title LIKE 'Student Question Title%';\
    `);
});

Cypress.Commands.add('cleanupGeneratedQuestions', () => {
    cy.queryDatabase(
        `WITH q_id AS (\
            SELECT id FROM questions WHERE title LIKE '%Student Question Title%'\
        ) DELETE FROM options WHERE question_id IN (SELECT id FROM q_id);\
        DELETE FROM questions WHERE title LIKE '%Student Question Title%';\
    `);
});
