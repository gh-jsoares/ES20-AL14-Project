package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class AddTopicToStudentQuestionSpockTest extends Specification {

    def "add existing topic to existing student question"() {
        // topic is added to student question
        expect: false
    }

    def "topic does not exist but studentquestion does"() {
        // throw an exception
        expect: false
    }

    def "topic exists but studentquestion does not"() {
        // throw an exception
        expect: false
    }

    def "try to add already added topic to student question"() {
        // throw an exception
        expect: false
    }
}