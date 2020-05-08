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
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class TournamentEnrollSpockPerformanceTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_NAME = "Tournament"
    public static final USERNAME = 'username'
    public static final USERNAMECREATOR = 'usernameCreator'

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    UserRepository userRepository

    def courseExecution
    def tournaments = []
    def users = []

    public static final int NUM_USERS = 1   // 1000
    public static final int NUM_TOURNS = 1  // 10000
    public static final int NUM_CALLS = 1   // 10000

    def setup() {

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

    }

    def "performance testing to get 1000 users enrolled in 10000 tournaments"() {
        given: "<NUM_USERS> users"
        1.upto(NUM_USERS, {createUser(it)})

        and: "<NUM_TOURNS> tournaments"
        1.upto(NUM_TOURNS, {createTournament()})

        when: "<NUM_CALLS> calls to enroll"
        for (User user : users) {
            1.upto(NUM_CALLS, {tournamentService.tournamentEnrollStudent(tournaments[it-1].getId(), user.getId())})
        }

        then: true
    }

    def createUser(it){
        def user = new User('name2', USERNAME + it, it, User.Role.STUDENT)
        userRepository.save(user)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        users.push(user)
    }

    def createTournament(){
        def tournament = new Tournament()
        tournament.setTitle(TOURNAMENT_NAME)
        tournament.setCreationDate(DateHandler.now())
        tournament.setAvailableDate(DateHandler.now().plusDays(1))
        tournament.setConclusionDate(DateHandler.now().plusDays(2))
        tournament.setCourseExecution(courseExecution)
        tournament.setState(Tournament.State.ENROLL)
        tournamentRepository.save(tournament)
        tournaments.push(tournament)
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
