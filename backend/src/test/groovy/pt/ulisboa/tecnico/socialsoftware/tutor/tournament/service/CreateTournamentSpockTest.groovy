package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@DataJpaTest
class CreateTournamentSpockTest extends Specification{
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String VERSION = 'B'
    public static final int QUEST_NUM = 1

    @Autowired
    TournamentRepository tournRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    TournamentService tournService

    def course
    def courseExecution
    def creationDate
    def availableDate
    def conclusionDate
    def dayOne
    def dayTwo
    def dayThree
    def formatter
    def tournDto
    def user
    def topic

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // create course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // create user
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)

        // create topic
        topic = new Topic()
        topic.setName("topic")
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)

        // create tournamentDto
        tournDto = new TournamentDto()
        tournDto.setTitle(TOURN_TITLE)
        tournDto.setNumberOfQuestions(QUEST_NUM)
        tournDto.setState(Tournament.State.ENROLL)
        tournDto.setScramble(true)
        dayOne = LocalDateTime.now()
        dayTwo = LocalDateTime.now().plusDays(1)
        dayThree = LocalDateTime.now().plusDays(2)
        tournDto.setAvailableDate(dayTwo.format(formatter))
        tournDto.setConclusionDate(dayThree.format(formatter))
        tournDto.setSeries(1)
        tournDto.setVersion(VERSION)
        tournDto.addTopic(new TopicDto(topic))

    }

    def "create a tournament"() {
        given: 'tournament with correct values'

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament in repository"
        tournRepository.count() == 1L

        and: "tournament with correct values"
        def result = tournRepository.findAll().get(0)
        result.getId() != null
        result.isScramble()
        result.getTitle() == TOURN_TITLE
        result.getCreator() != null
        result.getCreationDate().format(formatter) == dayOne.format(formatter)
        result.getAvailableDate().format(formatter) == dayTwo.format(formatter)
        result.getConclusionDate().format(formatter) == dayThree.format(formatter)
        result.getState() == Tournament.State.ENROLL
        result.getSeries() == 1
        result.getVersion() == VERSION
        result.getNumberOfQuestions() == QUEST_NUM

    }

    def "create a tournament empty name"() {
        given: "tournament with empty name"
        tournDto.setTitle(null)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L

    }

    def "create a tournament blank name"() {
        given: "tournament with blank name"
        tournDto.setTitle("")

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "create a tournament conclusionDate before availableDate"() {
        given: "tournament with conclusion before available"
        tournDto.setAvailableDate(dayThree.format(formatter))
        tournDto.setConclusionDate(dayTwo.format(formatter))

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "create a tournament no topics"() {
        given: "tournament without topics"
        tournDto.getTopics().clear()

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "create a tournament non-positive number of questions"() {
        given: "tournament without positive number of questions"
        tournDto.setNumberOfQuestions(num)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L

        where: "invalid values"
        num << [0, -1]
    }

    def "tournament creator not STUDENT"() {
        given: "tournament not created by a student"
        user.setRole(User.Role.TEACHER)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "tournament conclusion before creation"() {
        given: "tournament with conclusion before creation"
        tournDto.setConclusionDate(dayOne.format(formatter))
        tournDto.setCreationDate(dayTwo.format(formatter))
        tournDto.setAvailableDate(dayThree.format(formatter))

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "tournament available before creation"() {
        given: "tournament with availableDate before creation"
        tournDto.setAvailableDate(dayOne.format(formatter))
        tournDto.setCreationDate(dayTwo.format(formatter))
        tournDto.setConclusionDate(dayThree.format(formatter))

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user.getId())

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }


    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}