package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.AssessmentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicConjunctionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

import java.util.stream.Collectors

@DataJpaTest
class GenerateTournamentQuizSpockTest extends Specification {
    public static final String USER_NAME = "name"
    public static final String USER_USERNAME = "username"
    public static final String USER_NAME2 = "name2"
    public static final String USER_USERNAME2 = "username2"
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String TOPIC_NAME = 'topic name'
    public static final String TOPIC_NAME2 = 'topic name 2'

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
    AssessmentRepository assessmentRepository

    @Autowired
    TopicConjunctionRepository conjunctionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    QuizAnswerRepository answerRepository

    @Autowired
    TournamentService tournService

    def course
    def users = []
    def courseExecution
    def topics = []
    def tourn
    def questions = []

    def setup() {
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        createTopic(TOPIC_NAME)
        createTopic(TOPIC_NAME2)

        createQuestion(1, "Question Title1", topics[0])
        createQuestion(2, "Question Title2", topics[0])
        createQuestion(3, "Question Title3", topics[1])

        createStudent(USER_NAME, USER_USERNAME, 1)
        createStudent(USER_NAME2, USER_USERNAME2, 2)
    }

    def createTopic(name) {
        def topic = new Topic()
        topic.setName(name)
        topic.setCourse(course as Course)
        course.addTopic(topic)
        topicRepository.save(topic)
        topics.add(topic)
    }

    def createQuestion(key, title, topic) {
        def question = new Question()
        question.setKey(key)
        question.setContent("Question Content")
        question.setTitle(title)
        question.setStatus(Question.Status.AVAILABLE)
        question.setCourse(course as Course)
        question.addTopic(topic)
        questionRepository.save(question)
        questions.add(question)
    }

    def createStudent(name, username, key) {
        def user = new User(name, username, key, User.Role.STUDENT)
        user.addCourse(courseExecution as CourseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)
        users.add(user)
    }

    def createTournament(enrolls, numQuest, hasQuiz, isAvailable) {
        tourn = new Tournament()
        tourn.setTitle(TOURN_TITLE)
        tourn.setCourseExecution(courseExecution as CourseExecution)
        tourn.setState(Tournament.State.ENROLL)
        tourn.setScramble(true)

        tourn.setCreationDate(DateHandler.now().minusDays(2))
        if (isAvailable) tourn.setAvailableDate(DateHandler.now().minusDays(1))
        else tourn.setAvailableDate(DateHandler.now().plusDays(1))
        tourn.setConclusionDate(DateHandler.now().plusDays(2))

        tourn.addTopic(topics[0] as Topic)
        tourn.setCreator(users[0] as User)
        tourn.setNumberOfQuestions(numQuest)

        1.upto(enrolls, {tourn.addEnrolledStudent(users[it-1 as int])})

        if (hasQuiz) {
            def quiz = new Quiz()
            quizRepository.save(quiz)
            tourn.setQuiz(quiz)
        }

        tournRepository.save(tourn)
    }

    def "generate quiz successfully"() {
        given: "a tournament"
        createTournament(2, 1, false, true)

        when: "service call to generate quiz"
        tournService.generateTournamentQuiz(tourn.getId() as int)

        then: "entities in repository"
        quizRepository.count() == 1L
        assessmentRepository.count() == 1L
        conjunctionRepository.count() == 1L
        answerRepository.count() == 2L

        and: "quiz and quizAnswers with correct data"
        def quiz = tournRepository.findAll().get(0).getQuiz()
        quiz != null
        quiz.getScramble() == tourn.isScramble()
        quiz.getType() == Quiz.QuizType.TOURNAMENT

        quiz.getQuizAnswers().size() == tourn.getEnrolledStudents().size()
        def answerUsers = quiz.getQuizAnswers().stream()
                .map{answer -> answer.getUser()}.collect(Collectors.toList())
        answerUsers.contains(users[0])
        answerUsers.contains(users[1])

        quiz.getQuizQuestions().size() == 1
        def resQuizQuestion = quiz.getQuizQuestions().stream().collect(Collectors.toList()).get(0)
        questions[0].getQuizQuestions().size() + questions[1].getQuizQuestions().size() == 1
        questions[0].getQuizQuestions().contains(resQuizQuestion) || questions[1].getQuizQuestions().contains(resQuizQuestion)
    }


    @Unroll
    def "invalid parameters: enrolls=#enrolls | validId=#validId | hasQuiz=#hasQuiz | isAvailable=#isAvailable | numQuest=#numQuest || errorMessage=#errorMessage"() {
        given: "a tournament"
        createTournament(enrolls, numQuest, hasQuiz, isAvailable)

        when: "service call to generate quiz"
        def tournId = validId ? tourn.getId() : tourn.getId() + 1
        tournService.generateTournamentQuiz(tournId as int)

        then: "exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        and: "quiz not generated"
        if (!hasQuiz) assert tourn.getQuiz() == null

        and: "entities not in repository"
        if (!hasQuiz) assert quizRepository.count() == 0L
        assessmentRepository.count() == 0L
        conjunctionRepository.count() == 0L
        answerRepository.count() == 0L

        where:

        enrolls | validId   | hasQuiz   | isAvailable   | numQuest  || errorMessage
        1       | true      | false     | true          | 2         || ErrorMessage.TOURNAMENT_NOT_ENOUGH_ENROLLS
        2       | false     | false     | true          | 2         || ErrorMessage.TOURNAMENT_NOT_FOUND
        2       | true      | true      | true          | 2         || ErrorMessage.TOURNAMENT_QUIZ_ALREADY_GENERATED
        2       | true      | false     | false         | 2         || ErrorMessage.TOURNAMENT_NOT_AVAILABLE
        2       | true      | false     | true          | 4         || ErrorMessage.NOT_ENOUGH_QUESTIONS
    }



    @TestConfiguration
    static class TournamentServiceImplTestContextConfiguration {

        @Bean
        TournamentService tournamentService() {
            return new TournamentService()
        }

        @Bean
        StatementService statementService() {
            return new StatementService()
        }

        @Bean
        QuizService quizService() {
            return new QuizService()
        }

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
    }

}
