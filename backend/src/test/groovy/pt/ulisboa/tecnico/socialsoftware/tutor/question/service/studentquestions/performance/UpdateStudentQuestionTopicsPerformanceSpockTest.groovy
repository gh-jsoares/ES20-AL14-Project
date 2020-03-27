package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions.performance

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class UpdateStudentQuestionTopicsPerformanceSpockTest extends Specification {

    public static final int AMOUNT_OF_QUESTIONS = 1 // 1000
    public static final int AMOUNT_OF_TOPICS_PER_QUESTION = 1 // 100

    public static final String COURSE_NAME = "Software Architecture"
    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String TOPIC_NAME = "name"

    @Autowired
    StudentQuestionService studentQuestionService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    TopicRepository topicRepository

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

    def "update AMOUNT_OF_QUESTIONS student question with AMOUNT_OF_TOPICS_PER_QUESTION topics"() {
        given: "$AMOUNT_OF_QUESTIONS student questions in db"
        StudentQuestion[] studentQuestions = []

        0.upto(AMOUNT_OF_QUESTIONS, {
            studentQuestions += createStudentQuestion(QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
            print("\nCREATING QUESTION NUMBER: $it\n")
        })

        and: "$AMOUNT_OF_TOPICS_PER_QUESTION topics in db"
        Integer[] topics = []
        0.upto(AMOUNT_OF_TOPICS_PER_QUESTION, {
            topics += createTopic(TOPIC_NAME + it, course).getId()
            print("\nCREATING TOPIC NUMBER: $it\n")
        })

        when:
        0.upto(AMOUNT_OF_QUESTIONS, {
            studentQuestionService.updateStudentQuestionTopics(studentQuestions[it.intValue()].getId(), topics)
            print("\nTEST NUMBER: $it\n")
        })

        then:
        true
    }

    private StudentQuestion createStudentQuestion(String title, String content, String status) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(user)
        studentQuestionRepository.save(studentQuestion)
        studentQuestion.setCourse(course)
        course.addStudentQuestion(studentQuestion)
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