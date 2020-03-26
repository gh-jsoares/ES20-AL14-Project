package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

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
        tournament.setCreationDate(LocalDateTime.now())
        tournament.setAvailableDate(LocalDateTime.now().plusDays(1))
        tournament.setConclusionDate(LocalDateTime.now().plusDays(2))
        tournament.setCourseExecution(courseExecution)
        tournament.setState(Tournament.State.ENROLL)
        tournamentRepository.save(tournament)
        tournaments.push(tournament)
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService administrationService() {
            return new TournamentService()
        }

    }
}
