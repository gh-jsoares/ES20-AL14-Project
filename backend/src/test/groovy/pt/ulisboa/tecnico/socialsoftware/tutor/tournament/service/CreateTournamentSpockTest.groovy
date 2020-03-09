
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentDto
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



class CreateTournamentSpockTest extends Specification{
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    //public static final String QUESTION_CONTENT = 'question content'
    public static final String TOURN_TITLE = 'tourn title'
    //public static final String QUIZ_TITLE = 'quiz title'
    public static final String VERSION = 'B'
    public static final int QUEST_NUM = 1


    def tournService = new TournamentService()

    def course
    def courseExecution
    def creationDate
    def availableDate
    def conclusionDate
    def dayOne
    def dayTwo
    def dayThree
    def questionDto
    def formatter
    def tournDto
    def user
    //def userDto
    def topicDto
    def topic
    def question

    def setup() {
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        // create course execution
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecution.setId(1)

        // create user
        user = new User(USER_NAME, USER_USERNAME, 1, User.Role.STUDENT)
        user.addCourse(courseExecution)
        //userDto = new UserDto(user)
        courseExecution.addUser(user)

        // create questions
        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        course.addQuestion(question)
        questionDto = new QuestionDto(question)

        // create topics
        topic = new Topic()
        topic.setName("topic")
        topic.setCourse(course)
        topic.addQuestion(question)
        course.addTopic(topic)
        topicDto = new TopicDto(topic)

        // create tournamentDto
        tournDto = new TournamentDto()
        tournDto.setKey(1)
        tournDto.setTitle(TOURN_TITLE)
        //tournDto.setCreator(userDto)
        tournDto.setNumberOfQuestions(QUEST_NUM)
        tournDto.setState(Tournament.State.ENROLL)
        tournDto.setScramble(true)
        dayOne = LocalDateTime.now()
        dayTwo = LocalDateTime.now().plusDays(1)
        dayThree = LocalDateTime.now().plusDays(2)
        tournDto.setCreationDate(dayOne)
        tournDto.setAvailableDate(dayTwo)
        tournDto.setConclusionDate(dayThree)
        tournDto.setSeries(1)
        tournDto.setVersion(VERSION)

    }

    def "create a tournament"() {
        given: 'tournament with correct values'
        tournDto.setTitle(TOURN_TITLE)

        when: "service call to create tournament"
        def result = tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "the correct tournament object is created"
        //result.getId() != null
        result.getKey() != null
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
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

    }

    def "create a tournament blank name"() {
        given: "tournament with blank name"
        tournDto.setTitle("")

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    def "create a tournament end time not after start time"() {
        given: "tournament with end before start"
        tournDto.setAvailableDate(dayThree)
        tournDto.setConclusionDate(dayTwo)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    def "create a tournament no topics"() {
        given: "tournament without topics"
        tournDto.getTopics().clear()

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    def "create a tournament non-positive number of questions"() {
        given: "tournament without positive number of questions"
        tournDto.setNumberOfQuestions(num)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT

        where: "invalid values"
        num << [0, -1]
    }

    def "create a tournament insufficient questions in selected topics"() {
        given: "tournament with more questions than in chosen topic"
        tournDto.setNumberOfQuestions(QUEST_NUM+1)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    def "tournament creator not STUDENT"() {
        given: "tournament not created by a student"
        user.setRole(User.Role.TEACHER)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }

    def "tournament conclusion before creation"() {
        given: "tournament with conclusion before creation"
        tournDto.setConclusionDate(dayOne)
        tournDto.setCreationDate(dayTwo)
        tournDto.setAvailableDate(dayThree)

        when: "service call to create tournament"
        tournService.createTournament(courseExecution.getId(), tournDto, user)

        then: "tournament not created, exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.TOURNAMENT_NOT_CONSISTENT
    }


    /*@TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }
    }*/

}