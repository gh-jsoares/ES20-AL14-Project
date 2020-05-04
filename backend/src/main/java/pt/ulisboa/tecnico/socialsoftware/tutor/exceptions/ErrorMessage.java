package pt.ulisboa.tecnico.socialsoftware.tutor.exceptions;

public enum ErrorMessage {

    INVALID_ACADEMIC_TERM_FOR_COURSE_EXECUTION("Invalid academic term for course execution"),
    INVALID_ACRONYM_FOR_COURSE_EXECUTION("Invalid acronym for course execution"),
    INVALID_CONTENT_FOR_OPTION("Invalid content for option"),
    INVALID_CONTENT_FOR_QUESTION("Invalid content for question"),
    INVALID_NAME_FOR_COURSE("Invalid name for course"),
    INVALID_NAME_FOR_TOPIC("Invalid name for topic"),
    INVALID_SEQUENCE_FOR_OPTION("Invalid sequence for option"),
    INVALID_SEQUENCE_FOR_QUESTION_ANSWER("Invalid sequence for question answer"),
    INVALID_TITLE_FOR_ASSESSMENT("Invalid title for assessment"),
    INVALID_TITLE_FOR_QUESTION("Invalid title for question"),
    INVALID_URL_FOR_IMAGE("Invalid url for image"),
    INVALID_TYPE_FOR_COURSE("Invalid type for course"),
    INVALID_TYPE_FOR_COURSE_EXECUTION("Invalid type for course execution"),
    INVALID_AVAILABLE_DATE_FOR_QUIZ("Invalid available date for quiz"),
    INVALID_CONCLUSION_DATE_FOR_QUIZ("Invalid conclusion date for quiz"),
    INVALID_RESULTS_DATE_FOR_QUIZ("Invalid results date for quiz"),
    INVALID_TITLE_FOR_QUIZ("Invalid title for quiz"),
    INVALID_TYPE_FOR_QUIZ("Invalid type for quiz"),
    INVALID_QUESTION_SEQUENCE_FOR_QUIZ("Invalid question sequence for quiz"),

    ASSESSMENT_NOT_FOUND("Assessment not found with id %d"),
    COURSE_EXECUTION_NOT_FOUND("Course execution not found with id %d"),
    OPTION_NOT_FOUND("Option not found with id %d"),
    QUESTION_ANSWER_NOT_FOUND("Question answer not found with id %d"),
    QUESTION_NOT_FOUND("Question not found with id %d"),
    QUIZ_ANSWER_NOT_FOUND("Quiz answer not found with id %d"),
    QUIZ_NOT_FOUND("Quiz not found with id %d"),
    QUIZ_QUESTION_NOT_FOUND("Quiz question not found with id %d"),
    TOPIC_CONJUNCTION_NOT_FOUND("Topic Conjunction not found with id %d"),
    TOPIC_NOT_FOUND("Topic not found with id %d"),
    USER_NOT_FOUND("User not found with id %d"),
    COURSE_NOT_FOUND("Course not found with name %s"),

    CANNOT_DELETE_COURSE_EXECUTION("The course execution cannot be deleted %s"),
    USERNAME_NOT_FOUND("Username %d not found"),

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
    USER_NOT_ENROLLED("%s - Not enrolled in any available course"),
    QUIZ_NO_LONGER_AVAILABLE("This quiz is no longer available"),
    QUIZ_NOT_YET_AVAILABLE("This quiz is not yet available"),
    TEACHER_NOT_IN_COURSE_EXECUTION("This teacher is not in the course execution"),

    NO_CORRECT_OPTION("Question does not have a correct option"),
    NOT_ENOUGH_QUESTIONS("Not enough questions to create a quiz"),
    ONE_CORRECT_OPTION_NEEDED("Questions need to have 1 and only 1 correct option"),
    CANNOT_CHANGE_ANSWERED_QUESTION("Can not change answered question"),
    QUIZ_HAS_ANSWERS("Quiz already has answers"),
    QUIZ_ALREADY_COMPLETED("Quiz already completed"),
    DISCUSSION_ALREADY_ANSWERED("This request was already answered"),
    QUIZ_ALREADY_STARTED("Quiz was already started"),
    QUIZ_QUESTION_HAS_ANSWERS("Quiz question has answers"),
    FENIX_ERROR("Fenix Error"),
    AUTHENTICATION_ERROR("Authentication Error"),
    FENIX_CONFIGURATION_ERROR("Incorrect server configuration files for fenix"),

    ACCESS_DENIED("You do not have permission to view this resource"),
    CANNOT_OPEN_FILE("Cannot open file"),

    DISCUSSION_MESSAGE_EMPTY("The message is an empty or blank string."),
    DISCUSSION_NOT_FOUND("Discussion not found with id %d"),
    DISCUSSION_QUESTION_NOT_ANSWERED("Student with id %d tried to create a discussion about a question he didn't answer"),

    TOURNAMENT_NOT_FOUND("Tournament not found with id %d"),
    TOURNAMENT_NOT_CONSISTENT("Field %s of tournament is not consistent"),
    TOURNAMENT_IS_NULL("The tournament was not specified"),
    TOURNAMENT_USER_IS_NOT_STUDENT("The user with id %d is not a student"),
    TOURNAMENT_STUDENT_NOT_ENROLLED_IN_TOURNAMENT_COURSE("The user with id %d is not enrolled in the course execution of the tournament"),
    TOURNAMENT_NOT_OPEN("The tournament with id %d is not open"),
    TOURNAMENT_TOPIC_WRONG_COURSE("The topic with id %d doesn't belong to the same course as the tournament"),
    TOURNAMENT_USER_IS_NOT_CREATOR("The user with username %s is not the creator of the tournament"),
    TOURNAMENT_HAS_STARTED("The tournament with id %d is no longer in enrollment phase"),

    USER_IS_NULL("The user was not specified"),
    USER_IS_NOT_TEACHER("User with name %s is not a teacher"),
    USER_IS_NOT_STUDENT("User %d is not a student"),


    DUPLICATE_STUDENT_QUESTION("Duplicate student question: %s"),
    TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION("Student question should have only one correct option"),
    NO_CORRECT_OPTION_STUDENT_QUESTION("Student question must have one correct option"),
    TOO_FEW_OPTIONS_STUDENT_QUESTION("Student question cant have less than 4 options"),
    STUDENT_QUESTION_TITLE_IS_EMPTY("The student question title is empty"),
    STUDENT_QUESTION_CONTENT_IS_EMPTY("The student question content is empty"),
    STUDENT_QUESTION_STATUS_IS_EMPTY("The student question status is empty"),
    STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY("Options need to have content"),
    STUDENT_QUESTION_NOT_A_STUDENT("You need to be a student to create a student question"),
    STUDENT_QUESTION_TOPIC_NOT_FOUND("The topic needs to exist"),
    STUDENT_QUESTION_NOT_FOUND("The student question does not exist"),
    STUDENT_QUESTION_USER_NOT_FOUND("The user was not found"),
    STUDENT_QUESTION_STUDENT_NOT_CREATOR("The student is not the creator of the student question: %s"),
    STUDENT_QUESTION_NOT_A_TEACHER("The user is not a teacher"),
    STUDENT_QUESTION_NOT_AWAITING_APPROVAL("The student question '%s' is not awaiting approval"),
    STUDENT_QUESTION_REJECT_NO_EXPLANATION("To reject a student question you need to write an explanation"),
    TOO_MANY_OPTIONS_STUDENT_QUESTION("Student question cant have more than 4 options");




    public final String label;

    ErrorMessage(String label) {
        this.label = label;
    }
}
