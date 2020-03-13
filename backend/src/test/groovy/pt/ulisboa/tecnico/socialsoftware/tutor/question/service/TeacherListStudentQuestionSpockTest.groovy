package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
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
class TeacherListStudentQuestionSpockTest extends Specification {


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
    StudentQuestionRepository studentQuestionRepository

    def student
    def teacher

    def setup() {
        student = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
        teacher = createUser(2, USER_NAME, TEACHER_USERNAME, User.Role.TEACHER)
    }

    def "N student questions exist and are listed"() {
        given: "a teacher user's username"
        TEACHER_USERNAME

        and: "and a list of 5 student questions made by a student"
        1.upto(5, {
            createStudentQuestion(it.intValue(), QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name(), USER_USERNAME)
        })
        when:
        def result = studentQuestionService.listAllStudentQuestions(TEACHER_USERNAME)

        then: "the list returned has all the questions made by the students"
        result.size() == 5
        result.stream().allMatch({ sq -> (sq.getUsername() == USER_USERNAME) })
        and: "reverse sorted by creation date"
        0.upto(result.size() - 2, {
            def date_1 = result[it.intValue()].getCreationDateAsObject()
            def date_2 = result[it.intValue() + 1].getCreationDateAsObject()
            assert !date_1.isBefore(date_2)
        })
    }

    @Unroll
    def "invalid data user=#isUser | teacher=#isTeacher | errorMessage=#errorMessage"() {
        given: "a username"
        def username = createUsername(isUser, isTeacher)

        when:
        studentQuestionService.listAllStudentQuestions(username)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isUser | isTeacher || errorMessage
        false  | true      || STUDENT_QUESTION_USER_NOT_FOUND
        true   | false     || STUDENT_QUESTION_NOT_A_TEACHER
    }

    private String createUsername(boolean isUser, boolean isTeacher) {
        if (!isUser)
            return null
        if (!isTeacher)
            return student.getUsername()

        return teacher.getUsername()
    }

    private createStudentQuestion(int key, String title, String content, String status, String username) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(key)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(userRepository.findByUsername(username))
        studentQuestion.setCreationDate(generateRandomCreationDate())
        studentQuestionRepository.save(studentQuestion)
    }

    private User createUser(int key, String name, String username, User.Role role) {
        def user = new User(name, username, key, role)
        userRepository.save(user)
        user
    }

    private static generateRandomCreationDate() {
        LocalDateTime now = LocalDateTime.now()
        Random random = new Random()
        int year = 60 * 60 * 24 * 365
        return now.plusSeconds((long) random.nextInt(4 * year) - 2 * year) // +- 2 years
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
