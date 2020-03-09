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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    def userCreator
    def user
    def course
    def courseExecution
    def creationDate
    def availableDate
    def conclusionDate
    def result
    def tournament
    def formatter
    def error


    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        creationDate = LocalDateTime.now()
        availableDate = LocalDateTime.now()
        conclusionDate = LocalDateTime.now().plusDays(1)

        userCreator = new User('name', USERNAMECREATOR, 1, User.Role.STUDENT)
        userCreator.addCourse(courseExecution)
        courseExecution.addUser(userCreator)

        tournament = new Tournament();
        tournament.setTitle(TOURNAMENT_NAME)
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
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
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

    def "the tournament exists but is not open"() {
        //an exception is thrown
        given: "a closed tournament"
            tournament.setState(Tournament.State.CLOSED)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)

        and: "a student"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            userRepository.save(user)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_OPEN
    }

    def "the tournament exists and is open but the student is already enrolled"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
        and: "a student enrolled in the tournament"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            userRepository.save(user)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.DUPLICATE_USER
    }

    def "the tournament does not exist"() {
        //an exception is thrown
        given: "A tournamentDto"
            def tournamentDto = new TournamentDto()
        and: "a student"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            userRepository.save(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_FOUND
    }

    def "the user does not exist"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, null);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.USER_IS_NULL
    }

    def "the user is not a student"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
        def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)
        and: "a student not in the course execution"
            user = new User('name2', USERNAME, 1, User.Role.TEACHER)
            userRepository.save(user)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT
    }

    def "the student does not belong to the course execution of the tournament"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)
        and: "a student not in the course execution"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            userRepository.save(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            error = thrown(TutorException)
            error.getErrorMessage() == ErrorMessage.TOURNAMENT_STUDENT_NOT_ENROLLED_IN_TOURNAMENT_COURSE
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}