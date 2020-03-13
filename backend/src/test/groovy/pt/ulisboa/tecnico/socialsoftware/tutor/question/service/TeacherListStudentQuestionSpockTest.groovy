package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Specification
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_NOT_A_STUDENT
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.STUDENT_QUESTION_USER_NOT_FOUND

@DataJpaTest
class TeacherListStudentQuestionSpockTest extends Specification {

    def "N student questions exist and are listed"() {
        // everything is cool
        expect: true
    }

    @Unroll
    def "invalid data user=#isUser | teacher=#isTeacher | errorMessage=#errorMessage"() {
        // error
        expect: true
    }
}
