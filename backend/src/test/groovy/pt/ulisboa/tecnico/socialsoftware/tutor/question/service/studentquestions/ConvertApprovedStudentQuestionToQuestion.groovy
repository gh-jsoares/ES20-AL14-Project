package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class ConvertApprovedStudentQuestionToQuestion extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String TEACHER_NAME = "Prof Almeida"
    public static final String TEACHER_USERNAME = "prof"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String COURSE_NAME = "Arquitetura de Software"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    CourseRepository courseRepository

    User user
    User teacher
    StudentQuestion studentQuestion
    Course course

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        teacher = new User(TEACHER_NAME, TEACHER_USERNAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        studentQuestion = createStudentQuestion(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
        createOptions(studentQuestion, OPTION_CONTENT)
    }

    def "convert existing student question to question"() {
        given: "an existing student question"
        def studentQuestionId = studentQuestion.getId()

        and: "its awaiting approval"
        studentQuestion.status = StudentQuestion.Status.AWAITING_APPROVAL

        when:
        studentQuestionService.approveStudentQuestion(teacher.id, studentQuestionId)

        then: "a question is created"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getOptions().size() == 4
        result.getOptions().stream().allMatch({ o -> o.getContent() == OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
        result.getImage() == null
        result.getCourse().getId() == course.getId()
    }

    @Unroll
    def "invalid data: user=#isTeacher | studentQuestion=#isStudentQuestion | status=#isAwaitingApproval || errorMessage=#errorMessage"() {
        given: "a student question"
        def studentQuestionId = createStudentQuestion(isStudentQuestion, isAwaitingApproval)

        and: "a user"
        def teacherId = createTeacher(isTeacher)

        when:
        studentQuestionService.approveStudentQuestion(teacherId, studentQuestionId)

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isStudentQuestion | isAwaitingApproval | isTeacher || errorMessage
        false             | true               | true      || STUDENT_QUESTION_NOT_FOUND
        true              | false              | true      || STUDENT_QUESTION_NOT_AWAITING_APPROVAL
        true              | true               | false     || STUDENT_QUESTION_NOT_A_TEACHER
    }

    private int createTeacher(boolean isTeacher) {
        return isTeacher ? teacher.getId() : user.getId()
    }

    private int createStudentQuestion(boolean isStudentQuestion, boolean isAwaitingApproval) {
        if (isStudentQuestion) {
            if (isAwaitingApproval)
                studentQuestion.status = StudentQuestion.Status.AWAITING_APPROVAL
            else
                studentQuestion.status = StudentQuestion.Status.ACCEPTED
            return studentQuestion.getId()
        }
        return -1
    }

    private StudentQuestion createStudentQuestion(String title, String content, String status) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(1)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(user)
        studentQuestionRepository.save(studentQuestion)
        studentQuestion.setCourse(course)
        course.addStudentQuestion(studentQuestion)
        studentQuestion
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

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}
