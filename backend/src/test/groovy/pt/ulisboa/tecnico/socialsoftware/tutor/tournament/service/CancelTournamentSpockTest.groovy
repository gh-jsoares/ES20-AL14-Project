package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

@DataJpaTest
class CancelTournamentSpockTest extends Specification{

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURNAMENT_NAME = "Tournament"
    public static final USERNAME = 'username'
    public static final USERNAME_2 = 'username2'
    public static final TOPIC_NAME = 'topic'

    @Autowired
    TournamentService tournamentService

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    QuizRepository quizRepository

    def user
    def courseExecution
    def tournament
    def topic
    def quiz


    def setup() {

        def course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        createTopic(course)

        createQuiz(courseExecution)

        user = createUser('name', USERNAME, 1, User.Role.STUDENT)

        createTournament(courseExecution, topic, quiz)

    }

    def "the tournament both exists and has not started and the creator cancels the tournament"() {
        //the tournament is cancelled
        given: 'a tournament not closed'
        tournament.setState(Tournament.State.ENROLL)

        and: "a student who creates the tournament"
        addCreatorToTournament(user)

        and: "an enrolled student"
        tournament.addEnrolledStudent(user)
        user.addEnrolledTournament(tournament)

        when:
        tournamentService.cancelTournament(tournament.getId(), user.getId());

        then: "quiz is correctly removed"
        quizRepository.count() == 0L

        and: "Tournament is correctly removed from the creator"
        user.getCreatedTournaments().size() == 0

        and: "Tournament is correctly removed from enrolled students"
        user.getEnrolledTournaments().size() == 0

        and: "Tournament is correctly removed from topic"
        topic.getTournaments().size() == 0

        and: "Tournament is correctly removed from course execution"
        courseExecution.getTournaments().size() == 0

        and: "Tournament is removed"
        tournamentRepository.count() == 0L
    }

    def "the tournament both exists and has started and the creator cancels the tournament"() {
        //the tournament is cancelled
        given: "an enrolled student"
        tournament.setState(Tournament.State.ENROLL)
        tournament.addEnrolledStudent(user)
        user.addEnrolledTournament(tournament)

        and: 'a tournament not closed'
        tournament.setState(Tournament.State.ONGOING)

        and: "a student who creates the tournament"
        addCreatorToTournament(user)

        when:
        tournamentService.cancelTournament(tournament.getId(), user.getId());

        then: "quiz is correctly removed"
        quizRepository.count() == 0L

        and: "Tournament is correctly removed from the creator"
        user.getCreatedTournaments().size() == 0

        and: "Tournament is correctly removed from enrolled students"
        user.getEnrolledTournaments().size() == 0

        and: "Tournament is correctly removed from topic"
        topic.getTournaments().size() == 0

        and: "Tournament is correctly removed from course execution"
        courseExecution.getTournaments().size() == 0

        and: "Tournament is removed"
        tournamentRepository.count() == 0L
    }

    @Unroll
    def "invalid data in database where tournament is #isTournament, user is #isUser, and errorMessage id #errorMessage"() {
        given: "a user"
        user
        and: "a tournamentDto"
        def tournamentDto = isTournament ? (new TournamentDto(tournament)) : new TournamentDto()

        when:
        tournamentService.tournamentEnrollStudent(tournamentDto.getId() == null ? -1 : tournamentDto.getId(), isUser ? user.getId() : -1);

        then:
        def error = thrown(TutorException)
        error.errorMessage == errorMessage

        where:
        isTournament | isUser     || errorMessage
        false        | true       || ErrorMessage.TOURNAMENT_NOT_FOUND
        true         | false      || ErrorMessage.USER_NOT_FOUND
    }

    @Unroll
    def "Invalid arguments: #userRole | #tournamentState | #userCreator || errorMessage"() {
        given: "a user with role userRole"
        def user2 = createUser('name2', USERNAME_2, 2, userRole)
        and: "a tournament with state tournamentState"
        tournament.setState(tournamentState)
        and: "a user that created the tournament"
        addCreatorToTournament(isUserCreator ? user2 : user)

        when:
        tournamentService.cancelTournament(tournament.getId(), user2.getId());

        then:
        def error = thrown(TutorException)
        error.getErrorMessage() == errorMessage

        where:
        userRole         | tournamentState          | isUserCreator              || errorMessage
        User.Role.TEACHER| Tournament.State.ENROLL  | false                      || ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT
        User.Role.STUDENT| Tournament.State.CLOSED  | true                       || ErrorMessage.TOURNAMENT_NOT_OPEN
        User.Role.STUDENT| Tournament.State.ENROLL  | false                      || ErrorMessage.TOURNAMENT_USER_IS_NOT_CREATOR
    }

    private createTopic(Course course) {
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)
    }

    private createQuiz(CourseExecution courseExecution) {
        quiz = new Quiz()
        quiz.setKey(1)
        courseExecution.addQuiz(quiz)
        quiz.setCourseExecution(courseExecution)
        quizRepository.save(quiz)
    }

    private createTournament(CourseExecution courseExecution, Topic topic, Quiz quiz) {
        tournament = new Tournament();
        tournament.setTitle(TOURNAMENT_NAME)
        tournament.setCreationDate(LocalDateTime.now())
        tournament.setAvailableDate(LocalDateTime.now().plusDays(1))
        tournament.setConclusionDate(LocalDateTime.now().plusDays(2))
        tournamentRepository.save(tournament)
        tournament.setCourseExecution(courseExecution)
        tournament.addTopic(topic)
        tournament.setQuiz(quiz)
    }

    def createUser(name, username, key, userRole){
        System.out.println("HELLO")
        def user2 = new User(name, username, key, userRole)
        user2.addCourse(courseExecution)
        courseExecution.addUser(user2)
        userRepository.save(user2)
        return user2
    }

    def addCreatorToTournament(creator) {
            tournament.setCreator(creator)
            creator.addCreatedTournament(tournament)
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        AnswerService answerService() {
            return new AnswerService()
        }

        @Bean
        AnswersXmlImport answersXmlImport() {
            return new AnswersXmlImport()
        }

        @Bean
        QuestionService questionService() {
            return new QuestionService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}
