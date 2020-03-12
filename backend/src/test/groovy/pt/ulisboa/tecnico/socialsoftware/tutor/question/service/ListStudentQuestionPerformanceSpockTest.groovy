package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class ListStudentQuestionPerformanceSpockTest extends Specification {

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
    def user_2

    def setup() {
        user = createUser(1, USER_NAME, USER_USERNAME, User.Role.STUDENT)
        user_2 = createUser(2, USER_NAME, USER_USERNAME + "_2", User.Role.STUDENT)
    }

    def "performance testing to get 1000 student questions"() {
        given: "a valid username"
        def user = USER_USERNAME

        and: "and a list of 1000 student questions made by the student"
        1.upto(1000, {
            createStudentQuestion(it.intValue(), QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name(), user)
        })

        and: "and a list of 1000 student questions made by another student"
        def user2 = USER_USERNAME + "_2"
        1.upto(1000, {
            createStudentQuestion(it.intValue() + 1000, QUESTION_TITLE + it + 1000, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name(), user2)
        })

        when:
        def result = studentQuestionService.listStudentQuestions(user)

        then: "the returned list is correct"
        result.size() == 1000
        result.stream().allMatch({ sq -> (sq.getUsername() == user) })
        and: "reverse sorted by creation date"
        0.upto(result.size() - 2, {
            def date_1 = result[it.intValue()].getCreationDateAsObject()
            def date_2 = result[it.intValue() + 1].getCreationDateAsObject()
            assert !date_1.isBefore(date_2)
        })
    }

    private createStudentQuestion(int key, String title, String content, String status, String username) {
        def studentQuestion = new StudentQuestion()
        studentQuestion.setKey(key)
        studentQuestion.setTitle(title)
        studentQuestion.setContent(content)
        studentQuestion.setStatus(StudentQuestion.Status.valueOf(status))
        studentQuestion.setStudent(userRepository.findByUsername(username))
        studentQuestion.setCreationDate(generateRandomCreationDate())
        studentQuestionRepository.save(studentQuestion)
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
