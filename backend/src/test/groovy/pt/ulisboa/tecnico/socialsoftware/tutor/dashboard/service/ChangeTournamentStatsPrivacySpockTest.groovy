package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class ChangeTournamentStatsPrivacySpockTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String NAME = 'user'
    public static final String USERNAME = 'username'


    @Autowired
    CourseRepository courseRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    DashboardService dashboardService

    def courseExecution
    def user

    def setup() {
        // create course
        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

    }

    @Unroll
    def "Invalid Arguments: #userRole || errorMessage"() {
        given: "a user with role userRole"
        if (userRole != null)
            createUser(userRole)

        when:
        dashboardService.changeTournamentStatsPrivacy(userRole == null ? -1 : user.getId() as int)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        userRole            || errorMessage
        User.Role.TEACHER   || ErrorMessage.USER_IS_NOT_STUDENT
        null                || ErrorMessage.USER_NOT_FOUND
    }

    @Unroll
    def "student changes tournament privacy: #privacyChangedPrior || should"() {
        given: "a student"
        createUser(User.Role.STUDENT)
        and: "privacy changed prior #privacyChangedPrior"
        if (privacyChangedPrior)
            user.changeTournamentStatsPrivacy()

        when:
        dashboardService.changeTournamentStatsPrivacy(user.getId() as int)

        then:
        user.isAnonymizeTournamentStats() == should

        where:
        privacyChangedPrior   || should
        false                 || true
        true                  || false
    }

    def createUser(userRole){
        user = new User(NAME, USERNAME, 1, userRole)
        user.addCourse(courseExecution as CourseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)
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

        @Bean
        DashboardService dashboardService() {
            return new DashboardService()
        }
    }
}