package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
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
class TeacherAnswersStudentServiceSpockPerformanceTest extends Specification {

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
    public static final String TEACHER_ANSWER  = "answer"
    public static final String USER_NAME = "test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final Integer COUNT = 1;

    User[] students = new User[COUNT+1]
    User teacher
    Question[] questions = new Question[COUNT+1]
    QuestionAnswer[] questionAnswers = new QuestionAnswer[COUNT+1]
    Discussion[] discussions = new Discussion[COUNT+1]
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

    def "performance testing COUNT teachers answering discussions"() {
        given: "COUNT students, COUNT teachers, questions and discussions"
        createUser(COUNT+1, User.Role.TEACHER)
        1.upto(COUNT, {
            createUser(it, User.Role.STUDENT)
            createQuestion(it)
        })
        and: "COUNT discussions"
        1.upto(COUNT, {
            createDiscussion(it)
        })
        when:
        1.upto(COUNT, {
            DiscussionDto discussionDto = new DiscussionDto()
            discussionDto.setUserName(teacher.getUsername())
            discussionDto.setTeacherAnswer(TEACHER_ANSWER)
            discussionDto.setMessageFromStudent(MESSAGE)
            discussionService.teacherAnswersStudent(discussions[it].getId(), discussionDto)
        })
        then:
        true
    }

    def createUser(it, role) {
        User user = new User(USER_NAME, USER_NAME + it, it.intValue(), role)
        userRepository.save(user)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        if (role == User.Role.STUDENT)
            students[it] = user
        else
            teacher = user
    }

    def createQuestion(it) {
        def question = new Question()
        question.setKey(it)
        question.setCourse(course)

        Quiz quiz = new Quiz()
        quiz.setKey(it)
        quiz.setCourseExecution(courseExecution)
        courseExecution.addQuiz(quiz)
        QuizQuestion quizQuestion = new QuizQuestion(quiz, question, 0)
        QuizAnswer quizAnswer = new QuizAnswer(students[it], quiz)
        questionAnswers[it] = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        questions[it] = question
        questionRepository.save(question)
    }

    def createDiscussion(it) {
        def discussion = new Discussion()
        discussion.setId(it)
        discussion.setMessageFromStudent(MESSAGE)
        discussion.setQuestion(questions[it])
        discussion.setStudent(students[it])

        discussions[it] = discussion
        discussionRepository.save(discussion)

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