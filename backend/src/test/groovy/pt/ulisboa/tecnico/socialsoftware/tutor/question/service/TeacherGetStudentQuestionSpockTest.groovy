package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class TeacherGetStudentQuestionSpockTest extends Specification {

    def "student question exists"() {
        // cool
        expect: true
    }

    @Unroll
    def "invalid data studentQuestion=#isStudentQuestion | user=#isUser | teacher=#isTeacher | errorMessage=#errorMessage"() {
        // error
        expect: true
    }
}