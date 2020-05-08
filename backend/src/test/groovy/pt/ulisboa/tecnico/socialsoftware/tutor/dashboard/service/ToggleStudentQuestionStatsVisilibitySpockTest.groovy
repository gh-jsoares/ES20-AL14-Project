package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class ToggleStudentQuestionStatsVisilibitySpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String COURSE_NAME = "Arquitetura de Software"

    @Autowired
    DashboardService dashboardService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    User user
    Course course

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }

    def "toggle settings false"() {
        given: "a student with default visibility (true)"

        when: "toggle visilibity settings"
        dashboardService.toggleStudentQuestionStatsVisibility(user.getId())

        then:
        !user.getStudentQuestionStatsVisibility()

    }

    def "toggle settings true"() {
        given: "a student with false visibility"
        user.toggleStudentQuestionStatsVisibility()

        when: "toggle visilibity settings"
        dashboardService.toggleStudentQuestionStatsVisibility(user.getId())

        then:
        user.getStudentQuestionStatsVisibility()

    }

    @TestConfiguration
    static class DashboardServiceImplTestContextConfiguration {
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
        DashboardService DashboardService() {
            return new DashboardService()
        }
    }

}
