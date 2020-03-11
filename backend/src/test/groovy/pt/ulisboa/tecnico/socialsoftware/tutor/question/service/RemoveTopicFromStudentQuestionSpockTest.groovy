package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification


@DataJpaTest
class RemoveTopicFromStudentQuestionSpockTest extends Specification {

    def "remove existing topic from existing student question"() {
        // removes topic from student question
        expect: true
    }

    def "topic does not exist but studentquestion does"() {
        // throw an exception
        expect: true
    }

    def "topic exists but studentquestion does not"() {
        // throw an exception
        expect: true
    }

    def "try to remove topic not present in student question"() {
        // throw an exception
        expect: true
    }
}