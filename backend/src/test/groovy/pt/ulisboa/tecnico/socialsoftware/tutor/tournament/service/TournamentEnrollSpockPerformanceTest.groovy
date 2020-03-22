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

    def setup() {

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

    }

    def "performance testing to get 1000 users enrolled in 10000 tournaments"() {
        given: "10000 users"
            def users = new User[1000]
            1.upto(1000, {
                users[it-1] = new User('name2', USERNAME + it, it, User.Role.STUDENT)
                userRepository.save(users[it-1])
                users[it-1].addCourse(courseExecution)
                courseExecution.addUser(users[it-1])
            })
        and: "10000 tournaments"
            def tournaments = new Tournament[10000]
            1.upto(10000, {
                tournaments[it-1] = new Tournament()
                tournaments[it-1].setTitle(TOURNAMENT_NAME)
                tournaments[it-1].setCreationDate(LocalDateTime.now())
                tournaments[it-1].setAvailableDate(LocalDateTime.now().plusDays(1))
                tournaments[it-1].setConclusionDate(LocalDateTime.now().plusDays(2))
                tournaments[it-1].setCourseExecution(courseExecution)
                tournaments[it-1].setState(Tournament.State.ENROLL)
                tournamentRepository.save(tournaments[it-1])
            })

        when:
            for (User user: users) {
                1.upto(10000, {
                    tournamentService.tournamentEnrollStudent(tournaments[it-1].getId(), user.getId())
                })
            }
        then:
            true
    }

    @TestConfiguration
    static class ServiceImplTestContextConfiguration {

        @Bean
        TournamentService administrationService() {
            return new TournamentService()
        }

    }
}
