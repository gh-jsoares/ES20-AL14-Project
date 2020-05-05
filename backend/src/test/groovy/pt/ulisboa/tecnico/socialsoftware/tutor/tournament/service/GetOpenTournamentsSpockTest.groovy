package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class GetOpenTournamentsSpockTest extends Specification{
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
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
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    TournamentService tournService

    def courseExecution
    def days = []
    def topic
    def user

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

        // create dates in different days
        days.add(DateHandler.now().plusDays(1))
        days.add(DateHandler.now().plusDays(2))

    }

    def createTournament(state, stack) {
        if (state == null)
            return
        def tourn = new Tournament()
        tourn.setTitle(TOURN_TITLE)
        tourn.setNumberOfQuestions(QUEST_NUM)
        tourn.setState(state)
        tourn.setScramble(true)
        tourn.setCreationDate(DateHandler.now())
        tourn.setAvailableDate(days[0])
        tourn.setConclusionDate(days[1])
        tourn.setSeries(1)
        tourn.setVersion(VERSION)
        tourn.setCourseExecution(courseExecution)
        tourn.addTopic(topic)
        tourn.setCreator(user)
        tournRepository.save(tourn)

        if (state != Tournament.State.CLOSED) {
            stack.push(tourn)
        }
    }

    @Unroll
    def "layout of existing tournaments: #tourn1 | #tourn2 => #size"() {
        given: "existing tournaments"
        def stack = []
        createTournament(tourn1, stack)
        createTournament(tourn2, stack)

        when: "service call to get open tournaments"
        def result = tournService.getOpenTournaments(courseExecution.getId(), -1)

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
        tournService.getOpenTournaments(execId, -1)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }

    @Unroll
    def "check correct display of quiz answer: #isUserEnrolled | #tournamentState || #size | #shouldHaveQuizAnswer"() {
        given: "a tournament"
        def stack = []
        createTournament(Tournament.State.ENROLL, stack)
        def tournament = stack[0]
        and: "an enrolled user"
        enrollUserInTournament(tournament, isUserEnrolled)
        and: "a quiz for the tournament"
        def quiz = createTournamentQuiz(tournament)
        and: "a quiz answer for the user"
        def quizAnswer = new QuizAnswer(user,quiz)
        quizAnswerRepository.save(quizAnswer)
        user.addQuizAnswer(quizAnswer)
        and: "the tournament state"
        setTournamentState(tournament, tournamentState)

        when:
        def result = tournService.getOpenTournaments(courseExecution.getId(), user.getId())

        then: "only one tournament returned"
        result.size() == size
        and: "the correct quiz answer is returned"
        if (shouldHaveQuizAnswer)
            result.get(0).getStatementQuiz().getId() == quizAnswer.getId()
        else
            !shouldHaveQuizAnswer

        where:

        isUserEnrolled          | tournamentState           || size | shouldHaveQuizAnswer
        true                    | Tournament.State.ENROLL   || 1    | false
        false                   | Tournament.State.ENROLL   || 1    | false
        true                    | Tournament.State.ONGOING  || 1    | true
        false                   | Tournament.State.ONGOING  || 1    | false
        true                    | Tournament.State.CLOSED   || 0    | false
        false                   | Tournament.State.CLOSED   || 0    | false
    }

    Quiz createTournamentQuiz(tournament){
        def quiz = new Quiz()
        quizRepository.save(quiz)
        tournament.setQuiz(quiz)
        quiz.setTournament(tournament)
        return quiz
    }

    def enrollUserInTournament(tournament, isUserEnrolled){
        if (isUserEnrolled) {
            tournament.addEnrolledStudent(user)
            user.addEnrolledTournament(tournament)
        }
    }

    def setTournamentState(tournament, tournamentState){
        switch (tournamentState) {
            case Tournament.State.ONGOING:
                tournament.setState(Tournament.State.ONGOING)
                tournament.setAvailableDate(DateHandler.now())
                break
            case Tournament.State.CLOSED:
                tournament.setCreationDate(DateHandler.now().minusHours(2))
                tournament.setAvailableDate(DateHandler.now().minusHours(1))
                tournament.setConclusionDate(DateHandler.now())
                tournament.setState(Tournament.State.CLOSED)
                break
            default:
                tournament.setState(Tournament.State.ENROLL)
        }
    }



    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}