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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

import java.time.LocalDateTime

@DataJpaTest
class CreateTournamentSpockPerformanceTest extends Specification {
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
    TournamentService tournService

    def course
    def execs = []
    def topics = []
    def tournDto
    def user

    public static final int NUM_EXECS = 1   // 10000
    public static final int NUM_TOPICS = 1  // 10000
    public static final int NUM_CALLS = 1   // 100000

    def "performance testing to create <NUM_CALLS> tournaments"() {
        given: "a course"
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        and: "<NUM_EXECS> course executions available in DB"
        1.upto(NUM_EXECS, {createExecution(it)})

        and: "<NUM_TOPICS> topics available in DB"
        1.upto(NUM_TOPICS, {createTopic(it)})

        and: "a tournamentDto as template"
        createTournamentTemplate()

        and: "a student to create tournament"
        createStudent()

        when: "service call to create <NUM_CALLS> tournaments"
        1.upto(NUM_CALLS, {tournService.createTournament(execs[it%NUM_EXECS].getId(), customTournDto(it), user.getId())})

        then: true
    }

    def createExecution(it) {
        def courseExecution = new CourseExecution(course, ACRONYM+it, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        execs.push(courseExecution)
    }

    def createTopic(it) {
        def topic = new Topic()
        topic.setName(TOPIC_NAME+it)
        topic.setCourse(course)
        course.addTopic(topic)
        topicRepository.save(topic)

        topics.push(new TopicDto(topic))
    }

    def createTournamentTemplate() {
        def available = (DateHandler.toISOString(LocalDateTime.now().plusDays(1)))
        def conclusion = (DateHandler.toISOString(LocalDateTime.now().plusDays(2)))

        tournDto = new TournamentDto()
        tournDto.setTitle(TOURN_TITLE)
        tournDto.setNumberOfQuestions(QUEST_NUM)
        tournDto.setState(Tournament.State.ENROLL)
        tournDto.setScramble(true)
        tournDto.setAvailableDate(available)
        tournDto.setConclusionDate(conclusion)
    }

    def createStudent() {
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        1.upto(NUM_EXECS, {
            user.addCourse(execs[it-1])
            execs[it-1].addUser(user)
        })
        userRepository.save(user)
    }

    def customTournDto(it) {
        def size = Math.min(it%20+1, NUM_TOPICS)
        tournDto.getTopics().clear()
        for (int i = 0; i < size; i++) {
            tournDto.addTopic(topics[(it+i)%NUM_TOPICS])
        }
        return tournDto
    }



    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }
}
