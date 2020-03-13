package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_TITLE_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_CONTENT_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_STATUS_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.NO_CORRECT_OPTION_STUDENT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOO_FEW_OPTIONS_STUDENT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOO_MANY_OPTIONS_STUDENT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DUPLICATE_STUDENT_QUESTION

@DataJpaTest
class CreateStudentQuestionSpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'url'

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

    def "create student question with title and 4 options"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = createStudentQuestionDto(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a username"
        USER_USERNAME

        and: "4 optionId"
        createOptions(studentQuestionDto, OPTION_CONTENT, 4, 1)

        when:
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "the correct question is inside the repository"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == StudentQuestion.Status.AWAITING_APPROVAL
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getOptions().size() == 4
        result.getStudent().getUsername() == USER_USERNAME
        user.getStudentQuestions().contains(result)
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
        result.getImage() == null
    }

    def "create studentquestion with a valid image"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = createStudentQuestionDto(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a username"
        USER_USERNAME

        and: "4 optionId"
        createOptions(studentQuestionDto, OPTION_CONTENT, 4, 1)

        and: "an image"
        createImage(studentQuestionDto, URL)

        when:
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "the correct question is inside the repository"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == StudentQuestion.Status.AWAITING_APPROVAL
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getOptions().size() == 4
        result.getStudent().getUsername() == USER_USERNAME
        user.getStudentQuestions().contains(result)
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
        result.getImage().getId() != null
        result.getImage().getUrl() == URL
        result.getImage().getWidth() == 20
    }

    @Unroll
    def "invalid arguments: title=#title | content=#content | status=#status | optionContent=#optionContent || errorMessage=#errorMessage"() {
        given: "a studentquestiondto"
        def studentQuestionDto = createStudentQuestionDto(title, content, status)

        and: "4 optionId"
        createOptions(studentQuestionDto, optionContent, 4, 1)

        and: "a username"
        USER_USERNAME

        when: "create a student question with invalid data"
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        title          | content          | status                                          | optionContent  || errorMessage
        null           | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() | OPTION_CONTENT || STUDENT_QUESTION_TITLE_IS_EMPTY
        "      "       | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() | OPTION_CONTENT || STUDENT_QUESTION_TITLE_IS_EMPTY
        QUESTION_TITLE | null             | StudentQuestion.Status.AWAITING_APPROVAL.name() | OPTION_CONTENT || STUDENT_QUESTION_CONTENT_IS_EMPTY
        QUESTION_TITLE | "     "          | StudentQuestion.Status.AWAITING_APPROVAL.name() | OPTION_CONTENT || STUDENT_QUESTION_CONTENT_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | null                                            | OPTION_CONTENT || STUDENT_QUESTION_STATUS_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | "      "                                        | OPTION_CONTENT || STUDENT_QUESTION_STATUS_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() | null           || STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() | "      "       || STUDENT_QUESTION_OPTION_CONTENT_IS_EMPTY
    }

    @Unroll
    def "invalid data: isDuplicate=#isDuplicate | options=#numberOptions | correctOptions=#numberCorrectOptions | user=#isUser | student=#isStudent | errorMessage=#errorMessage"() {
        given: "a studentquestiondto"
        def studentQuestionDto = createStudentQuestionDto(isDuplicate)

        and: "options"
        createOptions(studentQuestionDto, OPTION_CONTENT, numberOptions, numberCorrectOptions)

        and: "a username"
        def username = createUsername(isUser, isStudent)

        when: "create a student question with invalid data"
        studentQuestionService.createStudentQuestion(username, studentQuestionDto)

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isDuplicate | numberOptions | numberCorrectOptions | isUser | isStudent || errorMessage
        true        | 4             | 1                    | true   | true      || DUPLICATE_STUDENT_QUESTION
        false       | 0             | 1                    | true   | true      || TOO_FEW_OPTIONS_STUDENT_QUESTION
        false       | 5             | 1                    | true   | true      || TOO_MANY_OPTIONS_STUDENT_QUESTION
        false       | 4             | 0                    | true   | true      || NO_CORRECT_OPTION_STUDENT_QUESTION
        false       | 4             | 2                    | true   | true      || TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION
        false       | 4             | 1                    | false  | true      || STUDENT_QUESTION_USER_NOT_FOUND
        false       | 4             | 1                    | true   | false     || STUDENT_QUESTION_NOT_A_STUDENT
    }

    private String createUsername(boolean isUser, boolean isStudent) {
        if (!isUser)
            return null
        if (!isStudent)
            user.setRole(User.Role.TEACHER)

        return user.getUsername()
    }

    private static createOptions(StudentQuestionDto studentQuestionDto, String content, int number_of_options, int number_of_correct) {
        def options = new HashSet<OptionDto>()
        for (int i = 0; i < number_of_options; i++) {
            def optionDto = new OptionDto()
            optionDto.setContent(content)

            if (number_of_correct-- > 0)
                optionDto.setCorrect(true)

            options.add(optionDto)
        }
        studentQuestionDto.setOptions(options)
    }

    private static createImage(StudentQuestionDto studentQuestionDto, String url) {
        def image = new ImageDto()
        image.setUrl(url)
        image.setWidth(20)
        studentQuestionDto.setImage(image)
    }

    private StudentQuestionDto createStudentQuestionDto(boolean isDuplicate) {
        if (isDuplicate)
            createStudentQuestion(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())

        return createStudentQuestionDto(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
    }

    private createStudentQuestion(String title, String content, String status) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(1)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(userRepository.findByUsername(USER_USERNAME))
        studentQuestionRepository.save(studentQuestion)
    }

    private static StudentQuestionDto createStudentQuestionDto(String title, String content, String status) {
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(title)
        studentQuestionDto.setContent(content)
        studentQuestionDto.setStatus(status)
        studentQuestionDto
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}