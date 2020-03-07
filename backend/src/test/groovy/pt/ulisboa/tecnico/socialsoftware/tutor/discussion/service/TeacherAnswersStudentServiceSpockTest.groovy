package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Specification

class TeacherAnswersStudentServiceSpockTest extends Specification {

    public static final String MESSAGE = "message"
    public static final String STUDENT_NAME = "student_test"
    public static final String TEACHER_NAME = "teacher_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final String TEACHER_ANSWER = "teacher_answer_test"


    DiscussionService discussionService
    Question question
    User student
    User teacher
    Course course
    CourseExecution courseExecution
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    QuestionAnswer questionAnswer

    def setup() {
        discussionService = new DiscussionService()
        CourseDto courseDto = new CourseDto()
        courseDto.setCourseType(Course.Type.TECNICO)
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(COURSE_ACRONYM)
        courseDto.setAcademicTerm(COURSE_ACADEMIC_TERM)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)

        courseExecution = new CourseExecution(course, courseDto.getAcronym(), courseDto.getAcademicTerm(), courseDto.getCourseType())
        question = new Question()
        question.setKey(1)
        question.setCourse(course)

        student = new User('student', STUDENT_NAME, 1, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
    }

    def "teacher from the same course execution answers student and there is no answer from teacher yet"() {
        given: "a discussion without a teacher answer"
        def discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussion.setMessageFromStudent(MESSAGE)

        and: "a teacher from the same course execution"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(teacher)

        when: "adding the answer from the teacher to the discussion"
        discussionService.teacherAnswersStudent(teacher, TEACHER_ANSWER)

        then: "the data are correct"
        discussion.getTeacherAnswer() != NULL
        discussion.getTeacherAnswer() == TEACHER_ANSWER
        discussion.getTeacher() != NULL
        discussion.getTeacher() == teacher
    }

    def "the teacher is not in the course execution that has the question the student has a clarification request about"() {
        given: "a discussion without a teacher answer"
        def discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussion.setMessageFromStudent(MESSAGE)

        and: "a teacher that does not belong in the same course execution"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(teacher, TEACHER_ANSWER)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.TEACHER_NOT_IN_COURSE_EXECUTION
    }

    def "teacher answers student and there is already an answer from a teacher"() {
        given: "a discussion with a teacher answer"
        def discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussion.setMessageFromStudent(MESSAGE)
        discussion.setTeacherAnswer(TEACHER_ANSWER)

        and: "a teacher"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(teacher, TEACHER_ANSWER)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_ALREADY_ANSWERED
    }

    def "teacher answers student with empty message"() {
        given: "a discussion with a teacher answer and a teacher"
        def discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussion.setMessageFromStudent(MESSAGE)
        discussion.setTeacherAnswer(msg)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(teacher, TEACHER_ANSWER)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.EMPTY_ANSWER

        where:
        msg << [null, "\n\n\n", "\t"]
    }
}