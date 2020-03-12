package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_STUDENT_NOT_CREATOR

@DataJpaTest
class GetStudentQuestionSpockTest extends Specification {

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

    def user

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)
    }


    def "student question exists"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "it has 4 options"
        createOptions(studentQuestion, OPTION_CONTENT)

        and: "a username of a student"
        def user = USER_USERNAME

        when:
        def result = studentQuestionService.getStudentQuestion(user, studentQuestion.getId())

        then: "data is correct"
        result != null
        result.getId() == studentQuestion.getId()
        result.getKey() == 1
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getStatus() == StudentQuestion.Status.AWAITING_APPROVAL.name()
        result.getOptions().size() == 4
        result.getStudent().getUsername() == USER_USERNAME
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
    }

    def "the student question does not exist"() {
        given: "an non existing student question"
        def studentQuestionId = -1

        and: "a username of a student"
        def user = USER_USERNAME

        when:
        studentQuestionService.getStudentQuestion(user, studentQuestionId)

        then: "data is correct"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_FOUND
    }

    def "the user does not exist"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a null user"
        def user = null

        when:
        studentQuestionService.getStudentQuestion(user, studentQuestion.getId())

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_USER_NOT_FOUND
    }

    def "the user is not a student"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a username of a non student account"
        user.setRole(User.Role.TEACHER)
        def user = USER_USERNAME

        when:
        studentQuestionService.getStudentQuestion(user, studentQuestion.getId())

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_A_STUDENT
    }

    def "the user is not the creator of the student question"() {
        given: "an existing student question"
        def studentQuestion = createStudentQuestion(1, QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a username of a student that isnt the creator of the question"
        def user = USER_USERNAME + "_2"

        when:
        studentQuestionService.getStudentQuestion(user, studentQuestion.getId())

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_STUDENT_NOT_CREATOR
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

    private static createOptions(StudentQuestion studentQuestion, String content) {
        def options = new HashSet<Option>()

        for (int i = 0; i < 4; i++) {
            def option = new Option()
            option.setContent(content)
            option.setStudentQuestion(studentQuestion)
            options.add(option)
            studentQuestion.addOption(option)
        }
        options.first().setCorrect(true)
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
