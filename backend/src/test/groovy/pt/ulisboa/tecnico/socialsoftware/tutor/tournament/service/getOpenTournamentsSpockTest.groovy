package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import spock.lang.Specification



class getOpenTournamentsSpockTest extends Specification{

    def setup() {

    }

    def "exists open tournaments"() {
        // answers with non empty list containing the open tournaments
        expect: false
    }

    def "there's no tournaments"() {
        // answers with empty list
        expect: false
    }

    def "exists tournaments but none are open"() {
        // answers with empty list
        expect: false
    }

    def "course execution doesn't exist"() {
        // throw exception
        expect: false
    }
}