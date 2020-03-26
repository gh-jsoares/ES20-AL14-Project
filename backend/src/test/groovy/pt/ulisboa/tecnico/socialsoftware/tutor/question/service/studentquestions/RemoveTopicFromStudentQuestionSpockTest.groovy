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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_FOUND

@DataJpaTest
class RemoveTopicFromStudentQuestionSpockTest extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "name"
    public static final String COURSE_NAME = "Arquitetura de Software"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    StudentQuestionRepository studentQuestionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    CourseRepository courseRepository

    User user
    StudentQuestion studentQuestion
    Course course
    Topic topic

    def setup() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        userRepository.save(user)

        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        studentQuestion = createStudentQuestion(QUESTION_TITLE, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
        topic = createTopic(TOPIC_NAME, course)
    }

    def "remove existing topic from existing student question"() {
        given: "an existing student question"
        def studentQuestionId = studentQuestion.getId()

        and: "an existing topic"
        def topicId = topic.getId()

        and: "the student question belongs to the topic"
        studentQuestion.addTopic(topic)

        and: "the list of topics not containing topicId"
        def topics = studentQuestion.topics.stream().map({ t -> t.id }).filter({ id -> id != topicId }).collect(Collectors.toList())
        def topicIds = new int[topics.size()]
        topicIds = topics.toArray(topicIds)

        when:
        studentQuestionService.updateStudentQuestionTopics(studentQuestionId, topicIds as Integer[])

        then: "the topic is removed"
        studentQuestionRepository.count() == 1L
        def result = studentQuestionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getTitle() == QUESTION_TITLE
        result.getTopics().size() == 0
        def resultTopic = topicRepository.findAll().get(0)
        resultTopic.getStudentQuestions().size() == 0
    }

    @Unroll
    def "student question does not exist"() {
        given: "an existing student question"
        def studentQuestionId = createStudentQuestion(false)

        and: "an existing topic"
        def topicId = topic.getId()

        and: "the list of topics not containing topicId"
        def topics = studentQuestion.topics.stream().map({ t -> t.id }).filter({ id -> id != topicId }).collect(Collectors.toList())
        def topicIds = new int[topics.size()]
        topicIds = topics.toArray(topicIds)

        when:
        studentQuestionService.updateStudentQuestionTopics(studentQuestionId, topicIds as Integer[])

        then:
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_FOUND
    }

    private int createStudentQuestion(boolean isStudentQuestion) {
        if (isStudentQuestion)
            return studentQuestion.getId()
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
        studentQuestion
    }

    private Topic createTopic(String name, Course course) {
        def topic = new Topic()
        topic.setName(name)
        topic.setCourse(course)
        topicRepository.save(topic)
        topic
    }

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }
}