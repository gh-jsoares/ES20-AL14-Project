package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class CreateStudentQuestionPerformanceSpockTest extends Specification {

    public static final int AMOUNT_OF_TESTS = 1

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

    User user
    Course course

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
    }

    def "create AMOUNT_OF_TESTS student question"() {
        given: "$AMOUNT_OF_TESTS studentquestionDTO"
        StudentQuestionDto[] studentQuestionDto = []

        0.upto(AMOUNT_OF_TESTS, {
            def sq = createStudentQuestionDto(QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
            createOptions(sq, OPTION_CONTENT, 4, 1)
            studentQuestionDto += sq
        })

        when:
        0.upto(AMOUNT_OF_TESTS, { studentQuestionService.createStudentQuestion(course.getId(), user.getId(), studentQuestionDto[it.intValue()]) })

        then:
        true
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