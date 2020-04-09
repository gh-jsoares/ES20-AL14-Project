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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_AWAITING_APPROVAL
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_TEACHER
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_REJECT_NO_EXPLANATION


@DataJpaTest
class TeacherRejectStudentQuestionSpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String TEACHER_USERNAME = "T_alcosta"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String EXPLANATION = "Not enough quality"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    OptionRepository optionRepository

    User student
    User teacher

    def setup() {
        student = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
        teacher = createUser(2, USER_NAME, TEACHER_USERNAME, User.Role.TEACHER)
    }

    def "a teacher rejects an existing student question awaiting approval, providing an explanation"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "it has 4 options"
        createOptions(studentQuestion, OPTION_CONTENT)

        when:
        def result = studentQuestionService.rejectStudentQuestion(teacher.getId(), studentQuestion.getId(), EXPLANATION)

        then: "data is correct"
        result != null
        result.getId() == studentQuestion.getId()
        result.getKey() == 1
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getStatus() == StudentQuestion.Status.REJECTED.name()
        result.getOptions().size() == 4
        result.getCreatorUsername() == USER_USERNAME
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
        result.getLastReviewerUsername() == TEACHER_USERNAME
        result.getRejectedExplanation() == EXPLANATION

        and: "teacher contains reviewed question"
        def resultTeacher = userRepository.findByUsername(TEACHER_USERNAME)
        resultTeacher.getReviewedStudentQuestions().size() == 1
        resultTeacher.getReviewedStudentQuestions()[0].getId() == result.getId()
    }

    @Unroll
    def "invalid data studentQuestion=#isStudentQuestion | awaitingApproval=#isAwaitingApproval | user=#isUser | teacher=#isTeacher | explanation=#explanation | errorMessage=#errorMessage"() {
        given: "a student question"
        def studentQuestionId = createStudentQuestion(isStudentQuestion, isAwaitingApproval)

        and: "a userId"
        def userId = createUserId(isUser, isTeacher)

        when:
        studentQuestionService.rejectStudentQuestion(userId, studentQuestionId, explanation)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isStudentQuestion | isAwaitingApproval | isUser | isTeacher | explanation || errorMessage
        false             | true               | true   | true      | EXPLANATION || STUDENT_QUESTION_NOT_FOUND
        true              | false              | true   | true      | EXPLANATION || STUDENT_QUESTION_NOT_AWAITING_APPROVAL
        true              | true               | false  | true      | EXPLANATION || STUDENT_QUESTION_USER_NOT_FOUND
        true              | true               | true   | false     | EXPLANATION || STUDENT_QUESTION_NOT_A_TEACHER
        true              | true               | true   | true      | null        || STUDENT_QUESTION_REJECT_NO_EXPLANATION
        true              | true               | true   | true      | "    "      || STUDENT_QUESTION_REJECT_NO_EXPLANATION
    }

    private int createStudentQuestion(boolean isStudentQuestion, boolean isAwaitingApproval) {
        if (!isStudentQuestion)
            return -1

        if (isAwaitingApproval)
            return createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name()).getId()

        return createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.REJECTED.name()).getId()
    }

    private int createUserId(boolean isUser, boolean isTeacher) {
        if (!isUser)
            return -1
        if (!isTeacher)
            return student.getId()

        return teacher.getId()
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
