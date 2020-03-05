import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class TournamentEnrollSpockTest extends Specification{

    def tournService

    def setup() {
        tournService = new TournamentService()
    }

    def "the tournament both exists and is open and create tournament enroll"() {
        //the tournament enroll is created for the username
        expect: false
    }

    def "the tournament exists but is not open"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament and tournament enroll both exist for the user"() {
        //an exception is thrown
        expect: false
    }

    def "the tournament does not exist"() {
        //an exception is thrown
        expect: false
    }

    def "the user does not exist"() {
        //an exception is thrown
        expect: false
    }

    def "the user is a student"() {
        //an exception is thrown
        expect: false
    }

}