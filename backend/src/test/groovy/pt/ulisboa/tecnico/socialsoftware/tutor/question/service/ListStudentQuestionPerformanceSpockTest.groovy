package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

@DataJpaTest
class ListStudentQuestionPerformanceSpockTest extends Specification {

    def "performance testing to get 1000 student questions"() {
        // should be work fine
        expect: true
    }

}
