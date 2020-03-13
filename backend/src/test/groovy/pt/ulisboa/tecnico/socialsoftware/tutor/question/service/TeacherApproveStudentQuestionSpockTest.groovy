package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class TeacherApproveStudentQuestionSpockTest extends Specification {

    def "a teacher approves an existing student question awaiting approval"() {
        // ok
        expect: true
    }

    @Unroll
    def "invalid data studentQuestion=#isStudentQuestion | awaitingApproval=#isAwaitingApproval | user=#isUser | teacher=#isTeacher | errorMessage=#errorMessage"() {
        // error
        expect: false
    }
}
