package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class GetOpenTournamentsSpockTest extends Specification{
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String TOPIC_NAME = 'topic name'
    public static final String VERSION = 'B'
    public static final int QUEST_NUM = 1


    TournamentService tournService = new TournamentService()

    def course
    def courseExecution
    //def days = []
    def formatter
    def topic
    def user

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // create course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        //courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.setId(1)
        //courseExecutionRepository.save(courseExecution)

        // create topic
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course)
        course.addTopic(topic)
        //topicRepository.save(topic)

        // create user
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        user.addCourse(courseExecution)
        courseExecution.addUser(user)
        //userRepository.save(user)

        // create dates in different days
        //days.add((LocalDateTime.now().plusDays(1)))
        //days.add((LocalDateTime.now().plusDays(2)))
        //days.add((LocalDateTime.now().plusDays(3)))
        //days.add((LocalDateTime.now().plusDays(4)))
    }

    def createTournament(state) {
        def tourn = new Tournament()
        tourn.setTitle(TOURN_TITLE)
        tourn.setNumberOfQuestions(QUEST_NUM)
        tourn.setState(state)
        tourn.setScramble(true)
        tourn.setCreationDate(LocalDateTime.now())
        //tourn.setAvailableDate(days[0])
        //tourn.setConclusionDate(days[1])
        tourn.setSeries(1)
        tourn.setVersion(VERSION)
        tourn.addTopic(topic)
        tourn.setCourseExecution(courseExecution)
        tourn.setCreator(user)
        //tournRepository.save(tourn)
        return tourn
    }

    def "only exists open tournaments"() {
        given: "some open tournaments"
        def stack = []
        stack.push(createTournament(Tournament.State.ENROLL))
        stack.push(createTournament(Tournament.State.ENROLL))
        stack.push(createTournament(Tournament.State.ONGOING))
        stack.push(createTournament(Tournament.State.ENROLL))

        when: "service call to get open tournaments"
        def result = tournService.getOpenTournaments(courseExecution.getId())

        then: "list with open tournaments is returned"
        result.size() == 4
        and: "tournaments in right order (newer creation first)"
        for (TournamentDto tournDto : result) {
            tournDto.getState() == stack.pop().getState()
        }
    }

    def "exists both open and closed tournaments"() {
        given: "some open tournaments"
        def stack = []
        stack.push(createTournament(Tournament.State.ENROLL))
        stack.push(createTournament(Tournament.State.ENROLL))
        createTournament(Tournament.State.CLOSED)
        stack.push(createTournament(Tournament.State.ONGOING))
        stack.push(createTournament(Tournament.State.ENROLL))

        when: "service call to get open tournaments"
        def result = tournService.getOpenTournaments(courseExecution.getId())

        then: "list with only open tournaments is returned"
        result.size() == 4
        and: "tournaments in right order (newer creation first)"
        for (TournamentDto tournDto : result) {
            tournDto.getState() == stack.pop().getState()
        }
    }

    def "there's no tournaments"() {
        given: "no tournaments"
        // nothing to be done

        when: "service call to get open tournaments"
        def lst = tournService.getOpenTournaments(courseExecution.getId())

        then: "empty list is returned"
        lst.size() == 0

    }

    def "exists tournaments but none are open"() {
        given: "open tournaments"
        createTournament(Tournament.State.CLOSED)
        createTournament(Tournament.State.CLOSED)

        when: "service call to get open tournaments"
        def lst = tournService.getOpenTournaments(courseExecution.getId())

        then: "empty list is returned"
        lst.size() == 0
    }

    def "course execution doesn't exist"() {
        given: "open tournaments but courseExecution doesn't exist"
        createTournament(Tournament.State.ENROLL)
        def execId = -1

        when: "service call to get open tournaments"
        tournService.getOpenTournaments(execId)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }
}