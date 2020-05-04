package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.MessageDto
import spock.lang.Specification

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository

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


@DataJpaTest
class CreateDiscussionPerformanceTest extends Specification {

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
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final String QUESTION_TITLE = "question_title_test"

    Integer discNum = 1
    User[] students = new User[discNum + 1]
    Question[] questions = new Question[discNum + 1]
    QuestionAnswer[] questionAnswers = new QuestionAnswer[discNum + 1]
    Course course
    CourseExecution courseExecution

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
    }

    def "create a discussion"() {
        given: "the students, questions and discussions"
        1.upto(discNum, {
            createUser(it)
            createQuestion(it)
        })
        when:
        1.upto(discNum, {
            DiscussionDto discussionDto = new DiscussionDto()
            discussionDto.setId(questionAnswers[it].getId())
            def messages = new ArrayList<MessageDto>()
            def message = new MessageDto()
            message.setMessage(MESSAGE)
            messages.add(message)
            discussionDto.setMessagesDto(messages)
            discussionService.createDiscussion(students[it].getId(), questions[it].getId(), discussionDto)
        })
        then:
        true
    }

    def createUser(it) {
        User user = new User(STUDENT_NAME, STUDENT_NAME + it, it, User.Role.STUDENT)
        userRepository.save(user)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        students[it] = user
    }

    def createQuestion(it) {
        def question = new Question()
        question.setKey(it)
        question.setCourse(course)
        question.setTitle(QUESTION_TITLE)

        Quiz quiz = new Quiz()
        quiz.setKey(it)
        quiz.setCourseExecution(courseExecution)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        courseExecution.addQuiz(quiz)
        QuizQuestion quizQuestion = new QuizQuestion(quiz, question, 0)
        QuizAnswer quizAnswer = new QuizAnswer(students[it], quiz)
        questionAnswers[it] = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        questions[it] = question
        questionRepository.save(question)
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
