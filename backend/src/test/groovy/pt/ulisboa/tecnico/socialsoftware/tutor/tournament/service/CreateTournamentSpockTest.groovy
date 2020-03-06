import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import spock.lang.Specification

class CreateTournamentSpockTest extends Specification{

    def tournService

    def setup() {
        tournService = new TournamentService()
    }

    def "create a tournament"() {
        //the tournament is created
        expect: false
    }

    def "create a tournament empty name"() {
        //an exception is thrown
        expect: false
    }

    def "create a tournament blank name"() {
        //an exception is thrown
        expect: false
    }

    def "create a tournament end time not after start time"() {
        //an exception is thrown
        expect: false
    }

    def "create a tournament no topics"() {
        //an exception is thrown
        expect: false
    }

    def "create a tournament non-positive number of questions"() {
        //an exception is thrown
        expect: false
    }

    def "create a tournament insufficient questions in selected topics"() {
        //an exception is thrown
        expect: false
    }

    def "tournament creator not STUDENT"() {
        //an exception is thrown
        expect: false
    }
}