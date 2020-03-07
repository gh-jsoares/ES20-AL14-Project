import org.springframework.beans.factory.annotation.Autowired
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto.QuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TournamentEnrollSpockTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    static final USERNAME = 'username'


    def userCreator
    def user
    def tournamentService
    def course
    def courseExecution
    def creationDate
    def availableDate
    def conclusionDate
    def result
    def tournament
    def formatter


    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        tournamentService = new TournamentService()

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)

        creationDate = LocalDateTime.now()
        availableDate = LocalDateTime.now()
        conclusionDate = LocalDateTime.now().plusDays(1)

        userCreator = new User('name', USERNAME, 1, User.Role.STUDENT)
        userCreator.addCourse(courseExecution)
        courseExecution.addUser(userCreator)

        tournament = new Tournament();
        tournament.setCourseExecution(courseExecution)

    }

    def "the tournament both exists and is open and student enrolls in tournament"() {
        //the tournament enroll is created for the username
        given: 'an open tournament'
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)

        and: "a student"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then: "student enrolled in the tournament"
            result.getNumberOfEnrolls == 1
            def students = tournamentService.getTournamentStudents(result)
            def userIn = false
            for (UserDto student: students)
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
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            thrown(TutorException)
    }

    def "the tournament exists and is open but the student is already enrolled"() {
        //an exception is thrown
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
        and: "a student enrolled in the tournament"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            thrown(TutorException)
    }

    def "the tournament does not exist"() {
        //an exception is thrown
        given: "A tournamentDto"
            def tournamentDto = new TournamentDto()
        and: "a student"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            thrown(TutorException)
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
            thrown(TutorException)
    }

    def "the user is not a student"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
        def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)
        and: "a student not in the course execution"
            user = new User('name2', USERNAME, 1, User.Role.TEACHER)
            user.addCourse(courseExecution)
            courseExecution.addUser(user)
        when:
        result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
        thrown(TutorException)
    }

    def "the student does not belong to the course execution of the tournament"() {
        given: "an open tournament"
            tournament.setState(Tournament.State.ENROLL)
        and: "a tournamentDto"
            def tournamentDto = new TournamentDto(tournament)
            tournamentDto.setNumberOfEnrolls(0)
        and: "a student not in the course execution"
            user = new User('name2', USERNAME, 1, User.Role.STUDENT)
        when:
            result = tournamentService.tournamentEnrollStudent(tournamentDto, user);
        then:
            thrown(TutorException)
    }

}