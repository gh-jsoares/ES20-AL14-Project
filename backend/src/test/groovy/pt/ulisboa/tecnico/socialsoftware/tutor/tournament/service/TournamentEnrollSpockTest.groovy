package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
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
class TournamentEnrollSpockTest extends Specification{
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

    def user
    def courseExecution
    def tournament


    def setup() {

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        def userCreator = new User('name', USERNAMECREATOR, 1, User.Role.STUDENT)
        userCreator.addCourse(courseExecution)
        courseExecution.addUser(userCreator)

        tournament = new Tournament();
        tournament.setTitle(TOURNAMENT_NAME)
        tournament.setCreationDate(LocalDateTime.now())
        tournament.setAvailableDate(LocalDateTime.now().plusDays(1))
        tournament.setConclusionDate(LocalDateTime.now().plusDays(2))
        tournamentRepository.save(tournament)
        tournament.setCourseExecution(courseExecution)

    }

    def "the tournament both exists and is open and student enrolls in tournament"() {
        //the tournament enroll is created for the username
        given: 'an open tournament'
        tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
        def tournamentDto = new TournamentDto(tournament)

        and: "a student"
        user = new User('name2', USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        when:
        def result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then: "student enrolled in the tournament"
        result.getNumberOfEnrolls() == 1
        def tournamentResult = tournamentRepository.findById(tournamentDto.getId()).get()
        tournamentResult != null
        def students = tournamentResult.getEnrolledStudents()
        def userIn = false
        for (User student: students)
            if (student.getUsername() == USERNAME)
                userIn = true
        userIn
    }

    @Unroll
    def "invalid data in database where tournament is #isTournament, user is #isUser, and errorMessage id #errorMessage"() {
        given: "a user"
            createUser(isUser, User.Role.STUDENT)
        and: "a tournamentDto"
            def tournamentDto = isTournament ? (new TournamentDto(tournament)) : new TournamentDto()

        when:
            tournamentService.tournamentEnrollStudent(tournamentDto, user);

        then:
            def error = thrown(TutorException)
            error.errorMessage == errorMessage

        where:
        isTournament | isUser     || errorMessage
        false        | true       || ErrorMessage.TOURNAMENT_IS_NULL
        true         | false      || ErrorMessage.USER_IS_NULL
    }

    @Unroll
    def "Invalid arguments: #userRole | #tournamentState | #studentEnrolledInCourse | #studentEnrolledInTournament || errorMessage"() {
        given: "a user with role userRole"
            createUser(true, userRole)
        and: "a tournament with state tournamentState"
            tournament.setState(tournamentState)
        and: "a user enrolled in the course execution"
            enrollUserInCourseExecution(isUserEnrolledInCourse)
        and: "a user enrolled in the tournament"
            enrollUserInTournament(isUserEnrolledInTournament)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)

        when:
            tournamentService.tournamentEnrollStudent(tournamentDto, user);

        then:
            def error = thrown(TutorException)
            error.getErrorMessage() == errorMessage

        where:
            userRole         | tournamentState          | isUserEnrolledInCourse  | isUserEnrolledInTournament  || errorMessage
            User.Role.TEACHER| Tournament.State.ENROLL  | true                    | false                       || ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT
            User.Role.STUDENT| Tournament.State.CLOSED  | true                    | false                       || ErrorMessage.TOURNAMENT_NOT_OPEN
            User.Role.STUDENT| Tournament.State.ONGOING | true                    | false                       || ErrorMessage.TOURNAMENT_NOT_OPEN
            User.Role.STUDENT| Tournament.State.ENROLL  | false                   | false                       || ErrorMessage.TOURNAMENT_STUDENT_NOT_ENROLLED_IN_TOURNAMENT_COURSE
            User.Role.STUDENT| Tournament.State.ENROLL  | true                    | true                        || ErrorMessage.DUPLICATE_USER
    }

    def createUser(isUser, userRole){
        if (isUser) {
            user = new User('name2', USERNAME, 1, userRole)
            userRepository.save(user)
        } else
            user = null
    }

    def enrollUserInCourseExecution(isUserEnrolledInCourse){
        if (isUserEnrolledInCourse) {
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        }
    }

    def enrollUserInTournament(isUserEnrolledInTournament){
        if (isUserEnrolledInTournament)
            tournament.addEnrolledStudent(user)
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}