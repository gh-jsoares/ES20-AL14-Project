package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class GetStudentQuestionSpockTest extends Specification {

    def "student question exists"() {
        // everything is fine
        expect: true
    }

    def "the student question does not exist"() {
        // an error occurs
        expect: true
    }

    def "the user does not exist"() {
        // an error occurs
        expect: true
    }

    def "the user is not a student"() {
        // an error occurs
        expect: true
    }

}
