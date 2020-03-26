package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_TEACHER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND

@DataJpaTest
class TeacherListStudentQuestionPerformanceSpockTest extends Specification {

    public static final int AMOUNT_OF_QUESTIONS = 1 // 10000
    public static final int AMOUNT_OF_TESTS = 1 // 1000

    public static final String COURSE_NAME = "Software Architecture"
    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String TEACHER_USERNAME = "T_alcosta"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    Course course
    User student
    User teacher

    def setup() {
        student = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
        teacher = createUser(2, USER_NAME, TEACHER_USERNAME, User.Role.TEACHER)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }

    def "list AMOUNT_OF_QUESTIONS student questions AMOUNT_OF_TESTS times"() {
        given: "$AMOUNT_OF_TESTS student questions in db"
        StudentQuestion[] studentQuestions = []

        0.upto(AMOUNT_OF_QUESTIONS, {
            studentQuestions += createStudentQuestion(QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
            print("\nCREATING QUESTION NUMBER: $it\n")
        })

        when:
        0.upto(AMOUNT_OF_TESTS, {
            studentQuestionService.listAllStudentQuestions(course.getId(), teacher.getId())
            print("\nTEST NUMBER: $it\n")
        })

        then:
        true
    }


    private StudentQuestion createStudentQuestion(String title, String content, String status) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(student)
        studentQuestionRepository.save(studentQuestion)
        studentQuestion.setCreationDate(generateRandomCreationDate())
        studentQuestion.setCourse(course)
        course.addStudentQuestion(studentQuestion)
        studentQuestion
    }

    private static generateRandomCreationDate() {
        LocalDateTime now = LocalDateTime.now()
        Random random = new Random()
        int year = 60 * 60 * 24 * 365
        return now.plusSeconds((long) random.nextInt(4 * year) - 2 * year) // +- 2 years
    }

    private User createUser(int key, String name, String username, User.Role role) {
        def user = new User(name, username, key, role)
        userRepository.save(user)
        user
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
