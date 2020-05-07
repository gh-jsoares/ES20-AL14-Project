package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizQuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification


@DataJpaTest
class GetTournamentStatsSpockTest extends Specification{
    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String TOURN_TITLE = 'tourn title'
    public static final String TOPIC_NAME = 'topic name'
    public static final String VERSION = 'B'


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
    QuestionRepository questionRepository

    @Autowired
    QuizRepository quizRepository

    @Autowired
    QuizAnswerRepository quizAnswerRepository

    @Autowired
    QuestionAnswerRepository questionAnswerRepository

    @Autowired
    OptionRepository optionRepository

    @Autowired
    QuizQuestionRepository quizQuestionRepository

    @Autowired
    DashboardService dashboardService

    def courseExecution
    def topic
    def users = []
    def course
    def tourns = []
    def questions = []
    def quizzes = []

    def setup() {
        // create course
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)

        // create course execution
        courseExecution = new CourseExecution(course, ACRONYM, ACADEMIC_TERM, Course.Type.TECNICO)
        courseExecutionRepository.save(courseExecution)

        // create topic
        topic = new Topic()
        topic.setName(TOPIC_NAME)
        topic.setCourse(course as Course)
        course.addTopic(topic)
        topicRepository.save(topic)

        // create questions
        1.upto(5, {createQuestion(it, "question"+it)})

        // create students
        1.upto(4, {createStudent("mame"+it, "username"+it, it)})

    }

    def createTournament(state, enrolls, quiz) {
        if (state == null)
            return
        def tourn = new Tournament()

        tourn.setCourseExecution(courseExecution as CourseExecution)
        tourn.setState(Tournament.State.ENROLL)
        1.upto(enrolls, {tourn.addEnrolledStudent(users[it-1 as int])})

        tourn.setCreationDate(DateHandler.now().minusDays(3))
        if (state == Tournament.State.ENROLL) {
            tourn.setAvailableDate(DateHandler.now().plusDays(1))
            tourn.setConclusionDate(DateHandler.now().plusDays(2))
        } else if (state == Tournament.State.ONGOING) {
            tourn.setAvailableDate(DateHandler.now().minusDays(1))
            tourn.setConclusionDate(DateHandler.now().plusDays(1))
        } else {
            tourn.setAvailableDate(DateHandler.now().minusDays(2))
            tourn.setConclusionDate(DateHandler.now().minusDays(1))
        }
        tourn.setState(state)

        tourn.setTitle(TOURN_TITLE)
        tourn.setNumberOfQuestions(5)
        tourn.setScramble(true)
        tourn.setSeries(1)
        tourn.setVersion(VERSION)
        tourn.addTopic(topic as Topic)
        tourn.setCreator(users[0] as User)

        if (quiz != null) tourn.setQuiz(quiz)

        tournRepository.save(tourn)
        tourns.add(tourn)
    }

    def createQuestion(key, title) {
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
        def user = new User(name, username, key as Integer, User.Role.STUDENT)
        user.addCourse(courseExecution as CourseExecution)
        courseExecution.addUser(user)
        userRepository.save(user)
        users.add(user)
    }

    def createQuiz() {
        def quiz = new Quiz()
        quiz.setCourseExecution(courseExecution)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setCreationDate(DateHandler.now())

        1.upto(questions.size(), {
            def quizQuestion = new QuizQuestion(quiz, questions[it-1] as Question, it as Integer)
            quizQuestionRepository.save(quizQuestion)
        })

        quiz.setType(Quiz.QuizType.TOURNAMENT.toString())
        quizRepository.save(quiz)
        quizzes.add(quiz)
    }

    def createAnswer(user, quiz, correct) {
        def quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(false)
        quizAnswer.setUsedInStatistics(false)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)

        if (correct == -1) return

        1.upto(questions.size(), {
            def option = new Option()
            option.setContent("content")
            option.setSequence(it as Integer)
            option.setQuestion(questions[it-1] as Question)
            option.setCorrect(it <= correct)
            optionRepository.save(option)

            def questionAnswer = new QuestionAnswer(quizAnswer, quiz.getQuizQuestions().toList().get(it-1), null, option, it as Integer)
            questionAnswerRepository.save(questionAnswer)
        })
        quizAnswer.setCompleted(true)
    }

    def "invalid user id"() {
        given: "tournament closed and answered tournament"
        createTournament(Tournament.State.CLOSED, 2, null)

        when: "service call with invalid user id"
        dashboardService.getTournamentStats(-1, courseExecution.getId())

        then: "exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.USER_NOT_FOUND
    }

    def "invalid execution id"() {
        given: "tournament closed and answered tournament"
        createTournament(Tournament.State.CLOSED, 2, null)

        when: "service call with invalid execution id"
        dashboardService.getTournamentStats(users[0].getId(), -1)

        then: "exception thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.COURSE_EXECUTION_NOT_FOUND
    }

    def "first call after conclusion with >1 enroll generates"() {
        given: "tournament after conclusion but still with enroll state"
        createTournament(Tournament.State.CLOSED, 2, null)
        tourns[0].setState(Tournament.State.ENROLL)

        when: "service call with invalid execution id"
        dashboardService.getTournamentStats(users[0].getId(), topic.getId())

        then: "tournament closed and quiz generated"
        def tourn = tournRepository.findAll().get(0)
        tourn.getQuiz() != null
        tourn.getState() == Tournament.State.CLOSED
        quizRepository.count() == 1L
        quizAnswerRepository.count() == 2L

    }

    def "first call after conclusion with <=1 enroll closes but doesnt generate"() {
        given: "tournament after conclusion but still with enroll state"
        createTournament(Tournament.State.CLOSED, 1, null)
        tourns[0].setState(Tournament.State.ENROLL)

        when: "service call with invalid execution id"
        dashboardService.getTournamentStats(users[0].getId(), topic.getId())

        then: "tournament closed but quiz not generated"
        def tourn = tournRepository.findAll().get(0)
        tourn.getQuiz() == null
        tourn.getState() == Tournament.State.CLOSED
        quizRepository.count() == 0L
        quizAnswerRepository.count() == 0L

    }

    def "not completed any tournament"() {
        given: "tournament answered by user 0"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 1, quizzes[0])
        createAnswer(users[0], quizzes[0], 2)

        when: "service call by user 1"
        def result = dashboardService.getTournamentStats(users[1].getId(), courseExecution.getId())

        then: "no concluded tournaments of any kind"
        result.getClosedTournaments().size() == 0
        result.getTotalTournaments() == 0
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 0
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 0
        result.getTotalWrongAnswers() == 0
        result.getScore() == 0
    }

    def "enrolled but not answered or concluded"() {
        given: "tournament ongoing and not answered"
        createQuiz()
        createTournament(Tournament.State.ONGOING, 1, quizzes[0])

        when: "service call by user 1"
        def result = dashboardService.getTournamentStats(users[1].getId(), courseExecution.getId())

        then: "no concluded tournaments of any kind"
        result.getClosedTournaments().size() == 0
        result.getTotalTournaments() == 0
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 0
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 0
        result.getTotalWrongAnswers() == 0
        result.getScore() == 0
    }

    def "enrolled and answered but not concluded"() {
        given: "tournament ongoing and not answered"
        createQuiz()
        createTournament(Tournament.State.ONGOING, 1, quizzes[0])
        createAnswer(users[0], quizzes[0], 2)

        when: "service call by user 1"
        def result = dashboardService.getTournamentStats(users[1].getId(), courseExecution.getId())

        then: "no concluded tournaments of any kind"
        result.getClosedTournaments().size() == 0
        result.getTotalTournaments() == 0
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 0
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 0
        result.getTotalWrongAnswers() == 0
        result.getScore() == 0
    }

    def "enrolled and concluded but not answered"() {
        given: "tournament closed with only answers from user 0"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 2, quizzes[0])
        createAnswer(users[0], quizzes[0], 2)
        createAnswer(users[1], quizzes[0], -1)

        when: "service call by user 1"
        def result = dashboardService.getTournamentStats(users[1].getId(), courseExecution.getId())

        then: "one unsolved tournament, ranking 0(= not answered)"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 0
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 0
        result.getTotalUnsolved() == 1
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 0
        result.getTotalWrongAnswers() == 0
        result.getScore() == 0
    }

    def "1st place"() {
        given: "tournament with 3 answers, 1st: 0 and 1, 2nd: 2"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 3, quizzes[0])
        createAnswer(users[0], quizzes[0], 4)
        createAnswer(users[1], quizzes[0], 4)
        createAnswer(users[2], quizzes[0], 2)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 1st place tournament"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 1
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 1
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 1
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 4
        result.getTotalWrongAnswers() == 1
        result.getScore() == 10
    }

    def "2nd place"() {
        given: "tournament with 3 answers, 1st: 1, 2nd: 0 and 2"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 3, quizzes[0])
        createAnswer(users[0], quizzes[0], 2)
        createAnswer(users[1], quizzes[0], 4)
        createAnswer(users[2], quizzes[0], 2)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 2nd place tournament"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 2
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 1
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 1
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 2
        result.getTotalWrongAnswers() == 3
        result.getScore() == 5
    }

    def "3rd place"() {
        given: "tournament with 3 answers, 1st: 1, 2nd: 2, 3rd: 0"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 3, quizzes[0])
        createAnswer(users[0], quizzes[0], 1)
        createAnswer(users[1], quizzes[0], 4)
        createAnswer(users[2], quizzes[0], 2)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 3rd place tournament"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 3
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 1
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 1
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 1
        result.getTotalWrongAnswers() == 4
        result.getScore() == 3
    }

    def "non top place"() {
        given: "tournament with 3 answers, 1st: 1, 2nd: 3, 3rd: 2, 4th: 0"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 4, quizzes[0])
        createAnswer(users[0], quizzes[0], 1)
        createAnswer(users[1], quizzes[0], 4)
        createAnswer(users[2], quizzes[0], 2)
        createAnswer(users[2], quizzes[0], 3)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 4th place tournament"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 4
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 0
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 1
        result.getTotalSolved() == 1
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 0
        result.getTotalCorrectAnswers() == 1
        result.getTotalWrongAnswers() == 4
        result.getScore() == 0
    }

    def "perfect score"() {
        given: "tournament with 3 answers, user 0 with all correct"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 2, quizzes[0])
        createAnswer(users[0], quizzes[0], 5)
        createAnswer(users[1], quizzes[0], 4)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 1st place tournament with all answers correct"
        result.getClosedTournaments().size() == 1
        result.getClosedTournaments().get(0).getRanking() == 1
        result.getTotalTournaments() == 1
        result.getTotalFirstPlace() == 1
        result.getTotalSecondPlace() == 0
        result.getTotalThirdPlace() == 0
        result.getTotalUnrankedPlace() == 0
        result.getTotalSolved() == 1
        result.getTotalUnsolved() == 0
        result.getTotalPerfect() == 1
        result.getTotalCorrectAnswers() == 5
        result.getTotalWrongAnswers() == 0
        result.getScore() == 15
    }

    def "multiple concluded with different ranks"() {
        given: "multiple tournaments answered"
        createQuiz()
        createTournament(Tournament.State.CLOSED, 2, quizzes[0])
        createAnswer(users[0], quizzes[0], -1)
        createAnswer(users[1], quizzes[0], 4)

        createQuiz()
        createTournament(Tournament.State.CLOSED, 1, quizzes[1])
        createAnswer(users[0], quizzes[1], 3)

        createQuiz()
        createTournament(Tournament.State.CLOSED, 3, quizzes[2])
        createAnswer(users[0], quizzes[2], 3)
        createAnswer(users[1], quizzes[2], 4)
        createAnswer(users[2], quizzes[2], 5)

        createQuiz()
        createTournament(Tournament.State.CLOSED, 3, quizzes[3])
        createAnswer(users[0], quizzes[3], 5)
        createAnswer(users[1], quizzes[3], 4)
        createAnswer(users[2], quizzes[3], 5)

        createQuiz()
        createTournament(Tournament.State.CLOSED, 2, quizzes[4])
        createAnswer(users[0], quizzes[4], 2)
        createAnswer(users[1], quizzes[4], 3)

        createQuiz()
        createTournament(Tournament.State.CLOSED, 4, quizzes[5])
        createAnswer(users[0], quizzes[5], 1)
        createAnswer(users[1], quizzes[5], 2)
        createAnswer(users[2], quizzes[5], 3)
        createAnswer(users[3], quizzes[5], 4)

        when: "service call by user 0"
        def result = dashboardService.getTournamentStats(users[0].getId(), courseExecution.getId())

        then: "one 1st, 2nd and 3rd, final score 18"
        result.getClosedTournaments().size() == 6
        result.getClosedTournaments().stream().anyMatch{tourn -> tourn.getRanking() == 1}
        result.getClosedTournaments().stream().anyMatch{tourn -> tourn.getRanking() == 2}
        result.getClosedTournaments().stream().anyMatch{tourn -> tourn.getRanking() == 3}
        result.getClosedTournaments().stream().anyMatch{tourn -> tourn.getRanking() == 4}
        result.getTotalTournaments() == 6
        result.getTotalFirstPlace() == 2
        result.getTotalSecondPlace() == 1
        result.getTotalThirdPlace() == 1
        result.getTotalUnrankedPlace() == 1
        result.getTotalSolved() == 5
        result.getTotalUnsolved() == 1
        result.getTotalPerfect() == 1
        result.getTotalCorrectAnswers() == 14
        result.getTotalWrongAnswers() == 11
        result.getScore() == 23
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

        @Bean
        DashboardService dashboardService() {
            return new DashboardService()
        }
    }
}