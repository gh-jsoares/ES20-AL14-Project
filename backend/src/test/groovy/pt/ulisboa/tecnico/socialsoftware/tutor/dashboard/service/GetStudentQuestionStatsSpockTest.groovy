package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class GetStudentQuestionStatsSpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String COURSE_NAME = "Arquitetura de Software"

    @Autowired
    DashboardService dashboardService

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    CourseRepository courseRepository

    User user
    StudentQuestion studentQuestion
    Course course

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }

    @Unroll
    def "multiple data: awaitingCount=#awaitingCount | rejectedCount=#rejectedCount | approvedCount=#approvedCount || rejected=#rejected | approved=#approved | total=#total | percentage=#percentage"() {

        given: "number of awaitingCount"
        createStudentQuestions(awaitingCount, StudentQuestion.Status.AWAITING_APPROVAL)

        and: "number of rejectedCount"
        createStudentQuestions(rejectedCount, StudentQuestion.Status.REJECTED)

        and: "number of approvedCount"
        createStudentQuestions(approvedCount, StudentQuestion.Status.ACCEPTED)

        when:
        def studentQuestionsStats = dashboardService.getStudentQuestionStats(user.getId())

        then:
        studentQuestionsStats.getTotal() == total
        studentQuestionsStats.getApproved() == approved
        studentQuestionsStats.getRejected() == rejected
        studentQuestionsStats.getPercentage() == percentage

        where:
        awaitingCount | rejectedCount | approvedCount || rejected | approved | percentage | total
        0             | 0             | 0             || 0        | 0        | 0          | 0
        1             | 0             | 0             || 0        | 0        | 0          | 1
        2             | 0             | 0             || 0        | 0        | 0          | 2
        0             | 1             | 0             || 1        | 0        | 0          | 1
        0             | 2             | 0             || 2        | 0        | 0          | 2
        0             | 0             | 1             || 0        | 1        | 100        | 1
        0             | 0             | 2             || 0        | 2        | 100        | 2
        1             | 0             | 1             || 0        | 1        | 50         | 2
        0             | 1             | 1             || 1        | 1        | 50         | 2
        1             | 1             | 1             || 1        | 1        | 33         | 3
    }

    private void createStudentQuestions(Integer count, StudentQuestion.Status status) {
        if (count != 0)
            1.upto(count, {
                studentQuestion = createStudentQuestion(
                        QUESTION_TITLE + it,
                        QUESTION_CONTENT,
                        status.name(),
                        it.toInteger()
                )
            })
    }

    private StudentQuestion createStudentQuestion(String title, String content, String status, Integer key) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(key)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(user)
        user.addStudentQuestion(studentQuestion)
        studentQuestionRepository.save(studentQuestion)
        studentQuestion.setCourse(course)
        course.addStudentQuestion(studentQuestion)
        studentQuestion
    }

    @TestConfiguration
    static class DashboardServiceImplTestContextConfiguration {
        @Bean
        DashboardService DashboardService() {
            return new DashboardService()
        }
    }

}
