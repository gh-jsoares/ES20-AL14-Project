package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_TITLE_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_CONTENT_IS_EMPTY
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_STATUS_IS_EMPTY

@DataJpaTest
class CreateStudentQuestionSpockTest extends Specification {

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

    def "create student question with title and 4 options"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name())
        and: "a username"
        USER_USERNAME

        and: "4 optionId"
        def options = new HashSet<OptionDto>()
        for (int i = 0; i < 4; i++) {
            def optionDto = new OptionDto()
            optionDto.setContent(OPTION_CONTENT)
            optionDto.setCorrect(false)
            options.add(optionDto)
        }
        options.first().setCorrect(true)
        studentQuestionDto.setOptions(options)

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
        result.getImage() == null
        result.getOptions().size() == 4
        result.getStudent().getUsername() == USER_USERNAME
        user.getStudentQuestions().contains(result)
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
    }

    def "invalid arguments: title=#title | content=#content | status=#status || errorMessage=#errorMessage"() {
        given: "a studentquestiondto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(title)
        studentQuestionDto.setContent(content)
        studentQuestionDto.setStatus(status)

        and: "a username"
        USER_USERNAME
        
        when: "create a student question with invalid data"
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        title          | content          | status                                          || errorMessage
        null           | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() || STUDENT_QUESTION_TITLE_IS_EMPTY
        "      "       | QUESTION_CONTENT | StudentQuestion.Status.AWAITING_APPROVAL.name() || STUDENT_QUESTION_TITLE_IS_EMPTY
        QUESTION_TITLE | null             | StudentQuestion.Status.AWAITING_APPROVAL.name() || STUDENT_QUESTION_CONTENT_IS_EMPTY
        QUESTION_TITLE | "     "          | StudentQuestion.Status.AWAITING_APPROVAL.name() || STUDENT_QUESTION_CONTENT_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | null                                            || STUDENT_QUESTION_STATUS_IS_EMPTY
        QUESTION_TITLE | QUESTION_CONTENT | "      "                                        || STUDENT_QUESTION_STATUS_IS_EMPTY
    }

    def "student question has no correct options"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "4 not correct optionId"
        def options = new HashSet<OptionDto>()
        for (int i = 0; i < 4; i++) {
            def optionDto = new OptionDto()
            optionDto.setContent(OPTION_CONTENT)
            optionDto.setCorrect(false)
            options.add(optionDto)
        }
        studentQuestionDto.setOptions(options)
        
        and: "a username"
        USER_USERNAME

        when:
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.NO_CORRECT_OPTION_STUDENT_QUESTION
    }

    def "student question has less than 4 options"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "less than 4 optionId"
        def options = new HashSet<OptionDto>()
        for (int i = 0; i < 3; i++) {
            def optionDto = new OptionDto()
            optionDto.setContent(OPTION_CONTENT)
            optionDto.setCorrect(false)
            options.add(optionDto)
        }
        options.first().setCorrect(true)
        studentQuestionDto.setOptions(options)

        and: "a username"
        USER_USERNAME

        when:
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.TOO_FEW_OPTIONS_STUDENT_QUESTION
    }

    def "student question has more than one correct option"() {
        given: "a studentquestiondto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "four correct optionId"
        def options = new HashSet<OptionDto>()
        for (int i = 0; i < 4; i++) {
            def optionDto = new OptionDto()
            optionDto.setContent(OPTION_CONTENT)
            optionDto.setCorrect(true)
            options.add(optionDto)
        }
        studentQuestionDto.setOptions(options)

        and: "a username"
        USER_USERNAME

        when: "create a student question with more than one correct option"
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.TOO_MANY_CORRECT_OPTIONS_STUDENT_QUESTION
    }

    def "student already created a question with that title"() {
        given: "a studentquestion exists"
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(1)
        studentQuestion.setTitle(QUESTION_TITLE)
        studentQuestion.setContent(QUESTION_CONTENT)
        studentQuestion.setStudent(userRepository.findByUsername(USER_USERNAME))
        studentQuestionRepository.save(studentQuestion)

        and: "a studentquestiondto with the same title"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name())

        and: "a username"
        USER_USERNAME

        when: "create another student question with the same title"
        studentQuestionService.createStudentQuestion(USER_USERNAME, studentQuestionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DUPLICATE_STUDENT_QUESTION
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {

        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}