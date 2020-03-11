package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import spock.lang.Unroll

@DataJpaTest
class TeacherAnswersStudentServiceSpockTest extends Specification {

    @Autowired
    DiscussionService discussionService

    @Autowired
    CourseService courseService

    @Autowired
    UserRepository userRepository

    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    public static final String MESSAGE = "message"
    public static final String STUDENT_NAME = "student_test"
    public static final String TEACHER_NAME = "teacher_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final String TEACHER_ANSWER = "teacher_answer_test"
    public static final String NON_EXISTING_TEACHER = "non_existing_teacher_test"

    Question question
    User student
    Course course
    CourseExecution courseExecution
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    QuestionAnswer questionAnswer
    Discussion discussion

    def setup() {
        CourseDto courseDto = new CourseDto()
        courseDto.setCourseType(Course.Type.TECNICO)
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(COURSE_ACRONYM)
        courseDto.setAcademicTerm(COURSE_ACADEMIC_TERM)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseService.createTecnicoCourseExecution(courseDto)
        courseExecution = courseExecutionRepository.findAll().get(0)
        question = new Question()
        question.setKey(1)
        question.setCourse(course)

        student = new User('student', STUDENT_NAME, 1, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizAnswer = new QuizAnswer(student, quiz)
        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        DiscussionDto discussionDto = new DiscussionDto()
        discussionDto.setMessageFromStudent(MESSAGE)
        discussion = new Discussion(student, question, discussionDto)

        questionRepository.save(question)
        userRepository.save(student)
        discussionRepository.save(discussion)

    }

    def "teacher from the same course execution answers student and there is no answer from teacher yet"() {
        given: "a discussionDto with an answer from a teacher from the same course execution"
        def discussionDto = new DiscussionDto()
        discussionDto.setId(discussion.getId())
        discussionDto.setMessage(TEACHER_ANSWER)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(teacher)
        discussionDto.setUserName(teacher.getUsername())
        userRepository.save(teacher)

        when: "adding the answer from the teacher to the discussion"
        discussionService.teacherAnswersStudent(discussionDto.getId(), discussionDto)

        then: "the data are correct"
        discussionRepository.count() == 1L
        def result = discussionRepository.findAll().get(0)
        result.getTeacherAnswer() != null
        result.getTeacherAnswer() == TEACHER_ANSWER
        result.getTeacher() != null
        result.getTeacher() == teacher
        result.getMessageFromStudent() != null
        result.getMessageFromStudent() == MESSAGE
        result.getStudent() != null
        result.getStudent() == student
    }

    def "the teacher is not in the course execution that has the question the student has a clarification request about"() {
        given: "a discussionDto with an answer from a teacher that is not in the same course execution"
        def discussionDto = new DiscussionDto()
        discussionDto.setId(discussion.getId())
        discussionDto.setMessage(TEACHER_ANSWER)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        discussionDto.setUserName(teacher.getUsername())
        userRepository.save(teacher)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(discussionDto.getId(), discussionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.TEACHER_NOT_IN_COURSE_EXECUTION
    }

    def "teacher answers student and there is already an answer from a teacher"() {
        given: "a discussionDto with an answer from a teacher"
        def discussionDto = new DiscussionDto()
        discussionDto.setId(discussion.getId())
        discussionDto.setMessage(TEACHER_ANSWER)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        discussionDto.setUserName(teacher.getUsername())
        userRepository.save(teacher)

        and: "the discussion was already answered"
        discussion.setTeacherAnswer(TEACHER_ANSWER)
        discussion.setTeacher(teacher)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(discussionDto.getId(), discussionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_ALREADY_ANSWERED
    }

    def "teacher tries to answer a non-existent discussion"() {
        given: "a discussionDto with an answer from a teacher from the same course execution"
        def discussionDto = new DiscussionDto()
        discussionDto.setId(-1)
        discussionDto.setMessage(TEACHER_ANSWER)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        discussionDto.setUserName(teacher.getUsername())
        userRepository.save(teacher)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(discussionDto.getId(), discussionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_NOT_FOUND
    }

    @Unroll
    def "invalid values answer=#answer, username=#username"() {
        given: "a discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setId(discussion.getId())
        discussionDto.setMessage(answer)

        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        discussionDto.setUserName(username)
        userRepository.save(teacher)

        when: "adding the answer from the teacher"
        discussionService.teacherAnswersStudent(discussionDto.getId(), discussionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == errorMessage

        where:
        answer         | username              || errorMessage
        null           | TEACHER_NAME          || ErrorMessage.EMPTY_ANSWER
        ""             | TEACHER_NAME          || ErrorMessage.EMPTY_ANSWER
        "   "          | TEACHER_NAME          || ErrorMessage.EMPTY_ANSWER
        TEACHER_ANSWER | NON_EXISTING_TEACHER  || ErrorMessage.USER_NOT_FOUND
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {

        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }

        @Bean
        CourseService courseService() {
            return new CourseService()
        }
    }
}