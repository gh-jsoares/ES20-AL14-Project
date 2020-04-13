package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository

import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import spock.lang.Unroll


@DataJpaTest
class CreateDiscussionTest extends Specification {

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

    public static final Integer INVALID_ID = -1
    public static final String INVALID_NAME = "invalid"
    public static final String MESSAGE = "message"
    public static final String STUDENT_NAME = "student_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"

    Question question
    User student
    Course course
    CourseExecution courseExecution
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    QuestionAnswer questionAnswer

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
        quiz.setType(Quiz.QuizType.GENERATED)
        courseExecution.addQuiz(quiz)
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizAnswer = new QuizAnswer(student, quiz)
        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        questionRepository.save(question)
        userRepository.save(student)
    }

    def "create a discussion"() {
        given: "the definition of the discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(student.getId())
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(questionAnswer.getId())

        when: "create discussion in the repository"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "get discussion from the repository"
        discussionRepository.findAll().size() == 1
        def result = discussionRepository.findAll().get(0)
        result.getId() != null
        result.getStudent().getKey() == student.getKey()
        result.getQuestion().getId() == question.getId()
        result.getQuestion().getDiscussions().contains(result)
        question.getDiscussions().contains(result)
        student.getDiscussions().contains(result)

        student.getRole() == User.Role.STUDENT
        courseExecution.getUsers().contains(student)
        courseExecution.getQuizzes().contains(quiz)
        quiz.getCourseExecution() == courseExecution
        quiz.getQuizQuestions().contains(quizQuestion)
        quizQuestion.getQuestion() == question
        student.getQuizAnswers().contains(quizAnswer)
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        quizAnswer.getQuiz() == quiz
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
    }

    def "create a discussion with the same student and question"() {
        given: "the definition of the discussion"
        Discussion discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussionRepository.save(discussion)
        and: "the creation of the DiscussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(student.getId())
        discussionDto.setMessageFromStudent("TEST")
        discussionDto.setId(questionAnswer.getId())

        when: "create another discussion with the same student and question"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DUPLICATE_DISCUSSION
    }

    @Unroll
    def "create a discussion with an empty message is #msg"() {
        given: "the creation of the discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(student.getId())
        discussionDto.setMessageFromStudent(msg)
        discussionDto.setId(questionAnswer.getId())

        when: "create the discussion"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_MESSAGE_EMPTY

        where:
        msg << [null, "\n\n\n", "\t", "", "    "]
    }

    def "create a discussion with invalid user"() {
        given: "the creation of the discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(INVALID_ID)
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(questionAnswer.getId())

        when: "creating the discussion"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "create a discussion with invalid question"() {
        given: "the creation of the discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(student.getId())
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(questionAnswer.getId())

        when: "creating the discussion"
        discussionService.createDiscussion(INVALID_ID, discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.QUESTION_NOT_FOUND
    }

    def "create a discussion with invalid questionAnswer"() {
        given: "the creation of the discussionDto"
        def discussionDto = new DiscussionDto()
        discussionDto.setUserId(student.getId())
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(INVALID_ID)

        when: "creating the discussion"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.QUESTION_ANSWER_NOT_FOUND
    }

    def "create a discussion before student submitting answer"() {
        given: "the definition of the discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(questionAnswer.getId())
        and: "the definition of the invalid user"
        User invalidStudent = new User('student2', "invalid student", 2, User.Role.STUDENT)
        invalidStudent.addCourse(courseExecution)
        courseExecution.addUser(invalidStudent)
        userRepository.save(invalidStudent)
        discussionDto.setUserId(invalidStudent.getId())

        when: "try to create discussion in the repository"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "discussion question not answered exception"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_QUESTION_NOT_ANSWERED
    }

    def "create a discussion with invalid user type"() {
        given: "the definition of the discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setMessageFromStudent(MESSAGE)
        discussionDto.setId(questionAnswer.getId())
        and: "the definition of the invalid user"
        User invalidUser = new User('user2', "invalid student", 2, User.Role.TEACHER)
        invalidUser.addCourse(courseExecution)
        courseExecution.addUser(invalidUser)
        userRepository.save(invalidUser)
        discussionDto.setUserId(invalidUser.getId())

        when: "try to create discussion in the repository"
        discussionService.createDiscussion(question.getId(), discussionDto)

        then: "discussion question not answered exception"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_STUDENT
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
