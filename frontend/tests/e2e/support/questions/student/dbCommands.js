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
