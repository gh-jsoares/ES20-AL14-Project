package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentEditStudentQuestionSpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
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
    OptionRepository optionRepository

    @Autowired
    CourseRepository courseRepository

    User user
    StudentQuestion studentQuestion
    Course course

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        studentQuestion = createStudentQuestion(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
        createOptions(studentQuestion, OPTION_CONTENT)
    }

    def "edit existing student question"() {
        given: "an existing student question"
        def studentQuestionId = studentQuestion.getId()

        and: "its rejected"
        studentQuestion.status = StudentQuestion.Status.REJECTED

        and: "the new changes"
        def studentQuestionDto = createEditStudentQuestionDto()

        when:
        studentQuestionService.editStudentQuestion(user.id, studentQuestionId, studentQuestionDto)

        then: "the student question is edited and awaiting approval"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == StudentQuestion.Status.AWAITING_APPROVAL
        result.getTitle() == "New " + QUESTION_TITLE
        result.getContent() == "New " + QUESTION_CONTENT
        result.getOptions().size() == 4
        result.getOptions().stream().allMatch({ o -> o.getContent() == "New " + OPTION_CONTENT })
        result.getOptions().stream().filter({ o -> o.getCorrect() }).count() == 1L
        result.getImage() == null
        result.getCourse().getId() == course.getId()
    }

    private StudentQuestionDto createEditStudentQuestionDto() {
        def studentQuestionDto = new StudentQuestionDto(studentQuestion)
        studentQuestionDto.setTitle("New " + QUESTION_TITLE)
        studentQuestionDto.setContent("New " + QUESTION_CONTENT)
        studentQuestionDto.getOptions().forEach({ option -> option.setContent("New " + OPTION_CONTENT) })
        studentQuestionDto
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
            option.setSequence(i)
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
