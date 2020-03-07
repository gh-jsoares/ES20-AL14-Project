package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import spock.lang.Specification

class TeacherAnswersStudentServiceSpockTest extends Specification {
    def discService

    def setup() {
        discService = new DiscussionService()
    }

    def "teacher answers student and there is no answer from teacher yet"() {
        //the answer is saved
        expect: false
    }

    def "the teacher is not in the course execution that has the question the student has request about "() {
        //an exception is thrown
        expect: false
    }

    def "teacher answers student and there is an answer from the teacher"() {
        //an exception is thrown
        expect: false
    }

    def "teacher receives a request with a blank request from the student"() {
        //an exception is thrown
        expect: false
    }

    def "teacher receives a request with no student associated to the request"() {
        //an exception is thrown
        expect: false
    }

    def "no teacher associated to the discussion"(){
        //an exception is thrown
        expect: false
    }
}