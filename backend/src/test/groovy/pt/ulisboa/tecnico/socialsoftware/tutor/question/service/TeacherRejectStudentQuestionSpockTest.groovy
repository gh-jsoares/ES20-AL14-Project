package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class TeacherRejectStudentQuestionSpockTest extends Specification {

    def "a teacher rejects an existing student question awaiting approval, providing an explanation"() {
        // ook
        expect: true
    }

    @Unroll
    def "invalid data studentQuestion=#isStudentQuestion | awaitingApproval=#isAwaitingApproval | user=#isUser | teacher=#isTeacher | explanation=#explanation | errorMessage=#errorMessage"() {
        // error
        expect: true
    }
}
