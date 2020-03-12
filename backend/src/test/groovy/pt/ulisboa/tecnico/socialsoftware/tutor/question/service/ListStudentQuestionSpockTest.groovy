package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class ListStudentQuestionSpockTest extends Specification {

    def "student has submitted n questions"() {
        // check if submitted list contains all questions
        expect: true
    }

    def "student does not exist"() {
        // throw an exception
        expect: true
    }

}
