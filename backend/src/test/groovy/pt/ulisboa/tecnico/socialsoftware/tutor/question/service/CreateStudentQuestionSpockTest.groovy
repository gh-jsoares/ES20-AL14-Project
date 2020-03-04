package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import spock.lang.Specification

class CreateStudentQuestionSpockTest extends Specification {
    def studentQuestionService

    def setup() {
        studentQuestionService = new StudentQuestionService()
    }

    def "create student question with title and 4 options"() {
        // a student question is created
        expect: false
    }

    def "invalid arguments: title=#title | option1=#option1 | option2=#option2 | option3=#option3 | option4=#option4 | correct-option=#correct-option || errorMessage=#errorMessage"() {
        // an exception is thrown
        expect: false
    }

    def "student question has more than one correct option"() {
        // an exception is thrown
        expect: false
    }

    def "student already created a question with that title"() {
        // an exception is thrown
        expect: false
    }
}