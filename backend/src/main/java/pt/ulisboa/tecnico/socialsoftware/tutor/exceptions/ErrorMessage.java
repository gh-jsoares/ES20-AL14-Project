package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;

public enum ErrorMessage {

    DISCUSSION_MESSAGE_EMPTY("The message is an empty or blank string."),
    DISCUSSION_NOT_FOUND("Discussion not found with id %d"),
    DISCUSSION_QUESTION_NOT_ANSWERED("Student with id %d tried to create a discussion about a question he didn't answer"),
    USER_NOT_ENROLLED_IN_COURSE("%s - Not enrolled in the same question course execution"),

    QUIZ_NOT_FOUND("Quiz not found with id %d"),
    QUIZ_QUESTION_NOT_FOUND("Quiz question not found with id %d"),
    QUIZ_ANSWER_NOT_FOUND("Quiz answer not found with id %d"),
    QUESTION_ANSWER_NOT_FOUND("Question answer not found with id %d"),
    OPTION_NOT_FOUND("Option not found with id %d"),
    QUESTION_NOT_FOUND("Question not found with id %d"),
    USER_NOT_FOUND("User not found with id %d"),
    TOPIC_NOT_FOUND("Topic not found with id %d"),
    ASSESSMENT_NOT_FOUND("Assessment not found with id %d"),
    TOPIC_CONJUNCTION_NOT_FOUND("Topic Conjunction not found with id %d"),
    TOURNAMENT_NOT_FOUND("Tournament not found with id %d"),
    COURSE_EXECUTION_NOT_FOUND("Course execution not found with id %d"),

    COURSE_NOT_FOUND("Course not found with name %s"),
    COURSE_NAME_IS_EMPTY("The course name is empty"),
    COURSE_TYPE_NOT_DEFINED("The course type is not defined"),
    COURSE_EXECUTION_ACRONYM_IS_EMPTY("The course execution acronym is empty"),
    COURSE_EXECUTION_ACADEMIC_TERM_IS_EMPTY("The course execution academic term is empty"),
    CANNOT_DELETE_COURSE_EXECUTION("The course execution cannot be deleted %s"),
    USERNAME_NOT_FOUND("Username %s not found"),

    QUIZ_USER_MISMATCH("Quiz %s is not assigned to student %s"),
    QUIZ_MISMATCH("Quiz Answer Quiz %d does not match Quiz Question Quiz %d"),
    QUESTION_OPTION_MISMATCH("Question %d does not have option %d"),
    COURSE_EXECUTION_MISMATCH("Course Execution %d does not have quiz %d"),

    DUPLICATE_DISCUSSION("Duplicate discussion"),
    DUPLICATE_TOPIC("Duplicate topic: %s"),
    DUPLICATE_USER("Duplicate user: %s"),
    DUPLICATE_COURSE_EXECUTION("Duplicate course execution: %s"),

    USERS_IMPORT_ERROR("Error importing users: %s"),
    QUESTIONS_IMPORT_ERROR("Error importing questions: %s"),
    TOPICS_IMPORT_ERROR("Error importing topics: %s"),
    ANSWERS_IMPORT_ERROR("Error importing answers: %s"),
    QUIZZES_IMPORT_ERROR("Error importing quizzes: %s"),

    QUESTION_IS_USED_IN_QUIZ("Question is used in quiz %s"),
    QUIZ_NOT_CONSISTENT("Field %s of quiz is not consistent"),
    USER_NOT_ENROLLED("%s - Not enrolled in any available course"),
    QUIZ_NO_LONGER_AVAILABLE("This quiz is no longer available"),
    QUIZ_NOT_YET_AVAILABLE("This quiz is not yet available"),
    TEACHER_NOT_IN_COURSE_EXECUTION("This teacher is not in the course execution"),

    USER_IS_NULL("The user was not specified"),
    TOURNAMENT_IS_NULL("The tournament was not specified"),
    TOURNAMENT_USER_IS_NOT_STUDENT("The user with id %d is not a student"),
    TOURNAMENT_STUDENT_NOT_ENROLLED_IN_TOURNAMENT_COURSE("The user with id %d is not enrolled in the course execution of the tournament"),
    TOURNAMENT_NOT_OPEN("The tournament with id %d is not open"),

    NO_CORRECT_OPTION("Question does not have a correct option"),
    NOT_ENOUGH_QUESTIONS("Not enough questions to create a quiz"),
    QUESTION_MISSING_DATA("Missing information for quiz"),
    QUESTION_MULTIPLE_CORRECT_OPTIONS("Questions can only have 1 correct option"),
    QUESTION_CHANGE_CORRECT_OPTION_HAS_ANSWERS("Can not change correct option of answered question"),
    QUIZ_HAS_ANSWERS("Quiz already has answers"),
    QUIZ_ALREADY_COMPLETED("Quiz already completed"),
    DISCUSSION_ALREADY_ANSWERED("This request was already answered"),
    EMPTY_ANSWER("The answer needs to have more than zero characters"),
    QUIZ_QUESTION_HAS_ANSWERS("Quiz question has answers"),
    FENIX_ERROR("Fenix Error"),
    AUTHENTICATION_ERROR("Authentication Error"),
    FENIX_CONFIGURATION_ERROR("Incorrect server configuration files for fenix"),
    USER_IS_NOT_TEACHER("User with name %s is not a teacher"),


    DUPLICATE_STUDENT_QUESTION("Duplicate student question: %s"),
    TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION("Student question should have only one correct option"),
    NO_CORRECT_OPTION_STUDENT_QUESTION("Student question must have one correct option"),
    STUDENT_QUESTION_MISSING_DATA("Missing information for student question"),
    TOO_FEW_OPTIONS_STUDENT_QUESTION("Student question must have 4 options"),
    STUDENT_QUESTION_TITLE_IS_EMPTY("The student question title is empty"),
    STUDENT_QUESTION_CONTENT_IS_EMPTY("The student question content is empty"),
    STUDENT_QUESTION_STATUS_IS_EMPTY("The student question status is empty"),
    STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY("Options need to have content"),
    STUDENT_QUESTION_NOT_A_STUDENT("You need to be a student to create a student question"),
    STUDENT_QUESTION_TOPIC_NOT_FOUND("The topic needs to exist"),
    STUDENT_QUESTION_NOT_FOUND("The student question does not exist"),
    STUDENT_QUESTION_TOPIC_ALREADY_ADDED("The student question already belongs to the topic: %s"),
    STUDENT_QUESTION_USER_NOT_FOUND("The user was not found"),
    STUDENT_QUESTION_TOPIC_NOT_PRESENT("The topic '%s' was not found in the student question"),

    TOURNAMENT_NOT_CONSISTENT("Field %s of tournament is not consistent"),


    ACCESS_DENIED("You do not have permission to view this resource"),
    CANNOT_OPEN_FILE("Cannot open file");

    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
