package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND

@DataJpaTest
class ListStudentQuestionSpockTest extends Specification {

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

    def "student has submitted n questions"() {
        given: "a valid username"
        def user = USER_USERNAME

        and: "and a list of 5 student questions"
        1.upto(5, {
            createStudentQuestion(QUESTION_TITLE + it, QUESTION_CONTENT, StudentQuestion.Status.AWAITING_APPROVAL.name())
        })

        when:
        def result = studentQuestionService.listStudentQuestions(user)

        then: "the returned list is correct"
        result.size() == 5
        and: "reverse sorted by creation date"
        1.upto(result.size() - 1, {
            def date_1 = LocalDateTime.parse(((StudentQuestionDto) result[it.intValue()]).getCreationDate())
            def date_2 = LocalDateTime.parse(((StudentQuestionDto) result[it.intValue() + 1]).getCreationDate())
            assert date_1.isAfter(date_2)
        })
    }

    def "student does not exist"() {
        given: "a null user"
        def user = null

        when:
        studentQuestionService.listStudentQuestions(user)

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_USER_NOT_FOUND
    }

    def "user is not a student"() {
        given: "a username of a non student user"
        user.setRole(User.Role.TEACHER)
        def user = USER_USERNAME

        when:
        studentQuestionService.listStudentQuestions(user)

        then: "an error occurs"
        def error = thrown(TutorException)
        error.errorMessage == STUDENT_QUESTION_NOT_A_STUDENT
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

    @TestConfiguration
    static class QuestionServiceImplTestContextConfiguration {
        @Bean
        StudentQuestionService studentQuestionService() {
            return new StudentQuestionService()
        }
    }

}
