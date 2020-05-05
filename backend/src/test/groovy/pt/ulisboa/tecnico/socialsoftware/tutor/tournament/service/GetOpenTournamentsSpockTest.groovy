package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class GetOpenTournamentsSpockTest extends Specification{
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String USER_NAME2 = "name2"
    public static final String USER_USERNAME2 = "username2"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String TOPIC_NAME = 'topic name'
    public static final String VERSION = 'B'
    public static final int QUEST_NUM = 1


    @Autowired
    TournamentRepository tournRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    TournamentService tournService

    def courseExecution
    def days = []
    def topic
    def user
    def user2

    def setup() {
        // create course
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // create topic
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)

        // create user
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)

        user2 = new User(USER_NAME2, USER_USERNAME2, 2, User.Role.STUDENT)
        user2.addCourse(courseExecution)
        courseExecution.addUser(user2)
        userRepository.save(user2)

        // create question
        def question = new Question()
        question.setKey(1)
        question.setContent("Question Content")
        question.setTitle("Question Title1")
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(course)
        question.addTopic(topic as Topic)
        questionRepository.save(question)

        // create dates in different days
        days.add(DateHandler.now().plusDays(1))
        days.add(DateHandler.now().plusDays(2))

    }

    def createTournament(state, stack) {
        if (state == null)
            return
        def tourn = new Tournament()

        tourn.setCourseExecution(courseExecution)
        tourn.setState(Tournament.State.ENROLL)
        tourn.addEnrolledStudent(user as User)
        tourn.addEnrolledStudent(user2 as User)

        tourn.setCreationDate(DateHandler.now().minusDays(3))
        if (state == Tournament.State.ENROLL) {
            tourn.setAvailableDate(DateHandler.now().plusDays(1))
            tourn.setConclusionDate(DateHandler.now().plusDays(2))
        } else if (state == Tournament.State.ONGOING) {
            tourn.setAvailableDate(DateHandler.now().minusDays(1))
            tourn.setConclusionDate(DateHandler.now().plusDays(1))
        } else {
            tourn.setAvailableDate(DateHandler.now().minusDays(2))
            tourn.setConclusionDate(DateHandler.now().minusDays(1))
        }
        tourn.setState(state)

        tourn.setTitle(TOURN_TITLE)
        tourn.setNumberOfQuestions(QUEST_NUM)
        tourn.setScramble(true)
        tourn.setSeries(1)
        tourn.setVersion(VERSION)
        tourn.addTopic(topic)
        tourn.setCreator(user)
        tournRepository.save(tourn)

        if (state != Tournament.State.CLOSED) {
            stack.push(tourn)
        }

        return tourn
    }

    @Unroll
    def "layout of existing tournaments: #tourn1 | #tourn2 => #size"() {
        given: "existing tournaments"
        def stack = []
        createTournament(tourn1, stack)
        createTournament(tourn2, stack)

        when: "service call to get open tournaments"
        def result = tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "check number of returned tournaments"
        result.size() == size
        and: "match ids and order of tournaments"
        for (TournamentDto tournDto : result) {
            assert tournDto.getId() == stack.pop().getId()
        }

        where:

        tourn1                  | tourn2                    || size
        null                    | null                      || 0
        Tournament.State.CLOSED | null                      || 0
        Tournament.State.CLOSED | Tournament.State.CLOSED   || 0
        Tournament.State.ENROLL | null                      || 1
        Tournament.State.ENROLL | Tournament.State.CLOSED   || 1
        Tournament.State.CLOSED | Tournament.State.ENROLL   || 1
        Tournament.State.ENROLL | Tournament.State.ENROLL   || 2
        Tournament.State.ENROLL | Tournament.State.ONGOING  || 2
        Tournament.State.ONGOING| Tournament.State.ONGOING  || 2
    }

    def "course execution doesn't exist"() {
        given: "open tournaments but courseExecution doesn't exist"
        createTournament(Tournament.State.ENROLL, [])
        def execId = -1

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(execId, user.getId())

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }

    def "first call after available date generates quiz"() {
        given: "tournament after available before first call"
        def tourn = createTournament(Tournament.State.ONGOING, [])
        tourn.setState(Tournament.State.ENROLL)

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "quiz generated"
        def tournament = tournRepository.findAll().get(0)
        tournament.getQuiz() != null

        and: "status updated"
        tournament.getState() == Tournament.State.ONGOING
    }

    def "first call only after conclusion date can also generate quiz"() {
        given: "tournament after conclusion before first call"
        def tourn = createTournament(Tournament.State.CLOSED, [])
        tourn.setState(Tournament.State.ENROLL)

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "quiz generated"
        def tournament = tournRepository.findAll().get(0)
        tournament.getQuiz() != null

        and: "status updated"
        tournament.getState() == Tournament.State.CLOSED
    }

    def "doesn't try to generate quiz if already generated"() {
        given: "tournament with only one enroll"
        def tourn = createTournament(Tournament.State.ONGOING, [])
        def quiz = new Quiz();
        quizRepository.save(quiz)
        tourn.setQuiz(quiz)

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "quiz generated"
        def tournament = tournRepository.findAll().get(0)
        tournament.getQuiz() == quiz

        and: "status unchanged"
        tournament.getState() == Tournament.State.ONGOING
    }

    def "doesn't try to generate quiz if not enough students"() {
        given: "tournament with only one enroll"
        def tourn = createTournament(Tournament.State.ONGOING, [])
        tourn.setState(Tournament.State.ENROLL)
        tourn.getEnrolledStudents().remove(user2)
        user2.getEnrolledTournaments().clear()

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "quiz generated"
        def tournament = tournRepository.findAll().get(0)
        tournament.getQuiz() == null

        and: "status updated"
        tournament.getState() == Tournament.State.CLOSED
    }


    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        StatementService statementService() {
            return new StatementService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }
    }
}