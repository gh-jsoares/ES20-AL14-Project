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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND

@DataJpaTest
class StudentListStudentQuestionSpockTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
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

    def user
    def user_2
    Course course

    def setup() {
        user = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
        user_2 = createUser(2, USER_NAME, USER_USERNAME + "_2", User.Role.STUDENT)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }

    def "student has submitted n questions"() {
        given: "a valid username"
        USER_USERNAME

        and: "and a list of 5 student questions made by the student"
        1.upto(5, {
            createStudentQuestion(it.intValue(), QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name(), USER_USERNAME)
        })

        and: "and a list of 5 student questions made by another student"
        1.upto(5, {
            createStudentQuestion(it.intValue() + 5, QUESTION_TITLE + it + 5, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name(), USER_USERNAME + "_2")
        })

        when:
        def result = studentQuestionService.listStudentQuestions(course.getId(), USER_USERNAME)

        then: "the list returned has only questions made by the student"
        result.size() == 5
        result.stream().allMatch({ sq -> (sq.getCreatorUsername() == USER_USERNAME) })
        and: "reverse sorted by creation date"
        0.upto(result.size() - 2, {
            def date_1 = result[it.intValue()].getCreationDateAsObject()
            def date_2 = result[it.intValue() + 1].getCreationDateAsObject()
            assert !date_1.isBefore(date_2)
        })
    }

    @Unroll
    def "invalid data user=#isUser | student=#isStudent | errorMessage=#errorMessage"() {
        given: "a username"
        def username = createUsername(isUser, isStudent)

        when:
        studentQuestionService.listStudentQuestions(course.getId(), username)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isUser | isStudent || errorMessage
        false  | true      || STUDENT_QUESTION_USER_NOT_FOUND
        true   | false     || STUDENT_QUESTION_NOT_A_STUDENT
    }

    private String createUsername(boolean isUser, boolean isStudent) {
        if (!isUser)
            return null
        if (!isStudent)
            user.setRole(User.Role.TEACHER)

        return user.getUsername()
    }

    private createStudentQuestion(int key, String title, String content, String status, String username) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(key)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(userRepository.findByUsername(username))
        studentQuestion.setCreationDate(generateRandomCreationDate())
        studentQuestion.setCourse(course)
        course.addStudentQuestion(studentQuestion)
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
