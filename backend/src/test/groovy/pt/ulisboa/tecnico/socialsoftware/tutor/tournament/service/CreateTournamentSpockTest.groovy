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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

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
    public static final String TOPIC_NAME = 'topic name'
    public static final String VERSION = 'B'
    public static final int QUEST_NUM = 1

    @Autowired
    TournamentRepository tournRepository

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
    def days = [:]
    def formatter
    def topic

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // create course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // create topic
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)

        // create dates in different days
        days["-2"] = (LocalDateTime.now().minusDays(2).format(formatter))
        days["-1"] = (LocalDateTime.now().minusDays(1).format(formatter))
        days["+1"] = (LocalDateTime.now().plusDays(1).format(formatter))
        days["+2"] = (LocalDateTime.now().plusDays(2).format(formatter))

    }

    def "create a tournament"() {
        given: 'tournament with correct values'
        def tournDto = createTournamentDto(TOURN_TITLE, QUEST_NUM, days["+1"], days["+2"])
        and: "a student as creator"
        def user = createUser(User.Role.STUDENT)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament in repository"
        tournRepository.count() == 1L

        and: "tournament with correct values"
        def result = tournRepository.findAll().get(0)
        result.getId() != null
        result.isScramble()
        result.getTitle() == TOURN_TITLE
        result.getCreationDate().format(formatter) == LocalDateTime.now().format(formatter)
        result.getAvailableDate().format(formatter) == days["+1"]
        result.getConclusionDate().format(formatter) == days["+2"]
        result.getState() == Tournament.State.ENROLL
        result.getSeries() == 1
        result.getVersion() == VERSION
        result.getNumberOfQuestions() == QUEST_NUM

        and: "creator stored correctly"
        result.getCreator() != null
        result.getCreator().getId() == user.getId()

        and: "topic stored correctly"
        result.getTopics().size() == 1
        for (Topic t : result.getTopics()) {
            assert t.getName() == TOPIC_NAME
        }

    }

    def "create a tournament no topics"() {
        given: "tournament without topics"
        def tournDto = createTournamentDto(TOURN_TITLE, QUEST_NUM, days["+1"], days["+2"])
        tournDto.getTopics().clear()
        and: "a student as creator"
        def user = createUser(User.Role.STUDENT)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }

    def "create a tournament invalid topic"() {
        given: "tournament with invalid topic"
        def tournDto = createTournamentDto(TOURN_TITLE, QUEST_NUM, days["+1"], days["+2"])
        tournDto.getTopics().get(0).setId(-1)
        and: "a student as creator"
        def user = createUser(User.Role.STUDENT)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOPIC_NOT_FOUND

        and: "tournament not in repository"
        tournRepository.count() == 0L
    }


    @Unroll
    def "invalid date order: #creation-creation #available-available #conclusion-conclusion"() {
        given: "a tournamentDto with wrong date order"
        def tournDto = createTournamentDto(TOURN_TITLE, QUEST_NUM, days[available], days[conclusion])
        and: "a student as creator"
        def user = createUser(User.Role.STUDENT)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        and: "tournament not in repository"
        tournRepository.count() == 0L

        where:
        available | conclusion  || errorMessage
        "+2"      | "+1"        || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        "-1"      | "+1"        || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        "+1"      | "-1"        || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        "-2"      | "-1"        || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        "-1"      | "-2"        || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    @Unroll
    def "invalid parameters: #title | #numQuest | #creator | #validDto | #validExecId || #errorMessage"() {
        given: "a tournamentDto"
        def tournDto = validDto ? createTournamentDto(title, numQuest, days["+1"], days["+2"]) : null
        and: "a user as creator"
        def user = createUser(creator)
        and: "a courseExecutionId"
        def execId = validExecId ? courseExecution.getId() : -1

        when: "service call to create tournament"
        tournService.createTournament(execId, tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        and: "tournament not in repository"
        tournRepository.count() == 0L

        where:

        title       | numQuest  | creator           | validDto  | validExecId   || errorMessage
        null        | QUEST_NUM | User.Role.STUDENT | true      | true          || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        "   "       | QUEST_NUM | User.Role.STUDENT | true      | true          || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        TOURN_TITLE | -1        | User.Role.STUDENT | true      | true          || ErrorMessage.TOURNAMENT_NOT_CONSISTENT
        TOURN_TITLE | QUEST_NUM | null              | true      | true          || ErrorMessage.USER_IS_NULL
        TOURN_TITLE | QUEST_NUM | User.Role.TEACHER | true      | true          || ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT
        TOURN_TITLE | QUEST_NUM | User.Role.STUDENT | false     | true          || ErrorMessage.TOURNAMENT_IS_NULL
        TOURN_TITLE | QUEST_NUM | User.Role.STUDENT | true      | false         || ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }

    def createTournamentDto(title, numQuest, available, conclusion) {
        def tournDto = new TournamentDto()
        tournDto.setTitle(title)
        tournDto.setNumberOfQuestions(numQuest)
        tournDto.setState(Tournament.State.ENROLL)
        tournDto.setScramble(true)
        tournDto.setAvailableDate(available)
        tournDto.setConclusionDate(conclusion)
        tournDto.setSeries(1)
        tournDto.setVersion(VERSION)
        tournDto.addTopic(new TopicDto(topic))
        return tournDto
    }

    def createUser(creator) {
        if (creator == null) {
            return null
        }
        def user = new User(USER_NAME, USER_USERNAME, 1, creator)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)
        return user
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }

}