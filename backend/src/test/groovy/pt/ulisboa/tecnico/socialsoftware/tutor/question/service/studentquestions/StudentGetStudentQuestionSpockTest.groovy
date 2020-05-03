package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_STUDENT_NOT_CREATOR

@DataJpaTest
class StudentGetStudentQuestionSpockTest extends Specification {

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
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    OptionRepository optionRepository

    User user

    def setup() {
        user = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
    }

    def "student question exists"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "it has 4 options"
        createOptions(studentQuestion, OPTION_CONTENT)

        when:
        def result = studentQuestionService.getStudentQuestion(user.getId(), studentQuestion.getId())

        then: "data is correct"
        result != null
        result.getId() == studentQuestion.getId()
        result.getKey() == 1
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getStatus() == StudentQuestion.Status.AWAITING_APPROVAL.name()
        result.getOptions().size() == 4
        result.getCreatorUsername() == USER_USERNAME
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
    }

    @Unroll
    def "invalid data studentQuestion=#isStudentQuestion | user=#isUser | student=#isStudent | studentQuestionCreator=#isCreator | errorMessage=#errorMessage"() {
        given: "a student question"
        def studentQuestionId = createStudentQuestion(isStudentQuestion)

        and: "a username"
        def userId = createUserId(isUser, isStudent, isCreator)

        when:
        studentQuestionService.getStudentQuestion(userId, studentQuestionId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isStudentQuestion | isUser | isStudent | isCreator || errorMessage
        false             | true   | true      | true      || STUDENT_QUESTION_NOT_FOUND
        true              | false  | true      | true      || STUDENT_QUESTION_USER_NOT_FOUND
        true              | true   | false     | true      || STUDENT_QUESTION_NOT_A_STUDENT
        true              | true   | true      | false     || STUDENT_QUESTION_STUDENT_NOT_CREATOR
    }

    private int createStudentQuestion(boolean isStudentQuestion) {
        if (isStudentQuestion)
                return createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name()).getId()
        return -1
    }

    private int createUserId(boolean isUser, boolean isStudent, boolean isCreator) {
        if (!isUser)
            return -1
        if(!isCreator)
            return createUser(2, USER_NAME, USER_USERNAME + "_2", User.Role.STUDENT).getId()
        if (!isStudent)
            user.setRole(User.Role.TEACHER)

        return user.getId()
    }


    private StudentQuestion createStudentQuestion(int key, String title, String content, String status) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(key)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(userRepository.findByUsername(USER_USERNAME))
        studentQuestion.setCreationDate(generateRandomCreationDate())
        studentQuestionRepository.save(studentQuestion)
        return studentQuestion
    }

    private createOptions(StudentQuestion studentQuestion, String content) {
        def options = new HashSet<Option>()

        for (int i = 0; i < 4; i++) {
            def option = new Option()
            option.setContent(content)
            option.setStudentQuestion(studentQuestion)
            option.setSequence(i)
            options.add(option)
            studentQuestion.addOption(option)
            optionRepository.save(option)
        }
        options.first().setCorrect(true)
    }

    private static generateRandomCreationDate() {
        LocalDateTime now = LocalDateTime.now()
        Random random = new Random()
        int year = 60 * 60 * 24 * 365
        return now.plusSeconds((long) random.nextInt(4 * year) - 2 * year) // +- 2 years
    }

    private User createUser(int key, String name, String username, User.Role role) {
        user = new User(name, username, key, role)
        userRepository.save(user)
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }

}
