package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class GetOpenTournamentsSpockPerformanceTest extends Specification {
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String TOPIC_NAME = 'topic name'
    public static final int QUEST_NUM = 1

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    @Autowired
    TopicRepository topicRepository

    @Autowired
    UserRepository userRepository

    @Autowired
    TournamentRepository tournamentRepository

    @Autowired
    TournamentService tournService

    def courseExecution
    def course
    def topic
    def user

    public static final NUM_TOURNS = 1	// 1000
	public static final NUM_CALLS = 1	// 100000

    def "performance testing to get <NUM_TOURNS> tournaments"() {
        given: "a course"
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        and: "a course execution"
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        and: "a topic"
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)

        and: "a student"
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)

        and: "<NUM_TOURNS> tournaments"
        1.upto(NUM_TOURNS, {createTournament(it)})


        when: "<NUM_CALLS> service calls"
        1.upto(NUM_CALLS, {tournService.getOpenTournaments(courseExecution.getId(), -1)})

        then: true
    }

    def createTournament(it) {
        def tourn = new Tournament()
        tourn.setTitle(TOURN_TITLE+it)
        tourn.setCreationDate(DateHandler.now().minusDays(1))
        tourn.setAvailableDate(DateHandler.now().plusDays(1))
        tourn.setConclusionDate(DateHandler.now().plusDays(2))
        tourn.setState(Tournament.State.ENROLL)
        tourn.setNumberOfQuestions(QUEST_NUM)
        tourn.setCreator(user)
        tourn.setCourseExecution(courseExecution)
        tourn.addTopic(topic)
        tournamentRepository.save(tourn)
    }

    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
