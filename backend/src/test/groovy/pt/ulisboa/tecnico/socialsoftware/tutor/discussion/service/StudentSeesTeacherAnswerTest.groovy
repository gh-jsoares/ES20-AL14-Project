package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Message
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.MessageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentSeesTeacherAnswerTest extends Specification {
    @Autowired
    DiscussionService discussionService

    @Autowired
    CourseService courseService

    @Autowired
    UserRepository userRepository

    @Autowired
    DiscussionRepository discussionRepository

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    public static final Integer INVALID_ID = -1
    public static final String MESSAGE = "message"
    public static final String STUDENT_NAME = "student_test"
    public static final String TEACHER_NAME = "teacher_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final String TEACHER_ANSWER = "teacher_answer_test"
    public static final String QUESTION_TITLE = "question_title_test"

    Question question
    User student
    User teacher
    Course course
    CourseExecution courseExecution
    Quiz quiz
    QuizQuestion quizQuestion
    QuizAnswer quizAnswer
    QuestionAnswer questionAnswer

    def setup() {
        CourseDto courseDto = new CourseDto()
        courseDto.setCourseType(Course.Type.TECNICO)
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(COURSE_ACRONYM)
        courseDto.setAcademicTerm(COURSE_ACADEMIC_TERM)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseService.createTecnicoCourseExecution(courseDto)
        courseExecution = courseExecutionRepository.findAll().get(0)
        question = new Question()
        question.setKey(1)
        question.setCourse(course)
        question.setTitle(QUESTION_TITLE)

        student = new User('student', STUDENT_NAME, 1, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setCourseExecution(courseExecution)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        courseExecution.addQuiz(quiz)
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizAnswer = new QuizAnswer(student, quiz)
        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        questionRepository.save(question)
        DiscussionDto discussionDto = new DiscussionDto()
        def messages = new ArrayList<MessageDto>()
        def message = new MessageDto()
        message.setMessage(MESSAGE)
        messages.add(message)
        discussionDto.setMessages(messages)

        teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(teacher)

        courseExecution.addUser(teacher)
        message.setUserName(teacher.getUsername())
    }

    def "student has two discussions with answers"() {
        given: "two answered discussions"
        DiscussionDto discussionDto = new DiscussionDto()
        def messages = new ArrayList<MessageDto>()
        def message = new MessageDto()
        message.setMessage(MESSAGE)
        message.setUserName(STUDENT_NAME)
        messages.add(message)
        discussionDto.setMessages(messages)

        createBasicDiscussion(student,question, discussionDto)
        createBasicDiscussion(student,question, discussionDto)

        when: "search for student discussions"
        def result = discussionService.getDiscussionStudent(student.getId())

        then: "the returned data is correct"
        result.size() == 2
        result.get(0).getMessages().size() == 2
        result.get(1).getMessages().size() == 2
        result.get(0).getMessages().get(0).getMessage() == MESSAGE
        result.get(1).getMessages().get(0).getMessage() == MESSAGE
        result.get(0).getMessages().get(1).getUserName() == teacher.getUsername()
        result.get(0).getMessages().get(1).getMessage() == TEACHER_ANSWER
        result.get(1).getMessages().get(1).getUserName() == teacher.getUsername()
        result.get(1).getMessages().get(1).getMessage() == TEACHER_ANSWER
    }

    def "student has one discussion without answer"() {
        given: "one unanswered discussion"
        DiscussionDto discussionDto = new DiscussionDto()
        def messages = new ArrayList<MessageDto>()
        def message = new MessageDto()
        message.setMessage(MESSAGE)
        message.setUserName(STUDENT_NAME)
        messages.add(message)
        discussionDto.setMessages(messages)
        new Discussion(student, question, discussionDto)

        when: "search for student discussions"
        def result = discussionService.getDiscussionStudent(student.getId())

        then: "the returned data is correct"
        result.size() == 1
        result.get(0).getMessages().size() == 1
        userRepository.findByUsername(result.get(0).getMessages().get(0).getUserName()).getRole() == User.Role.STUDENT
    }

    def "student doesn't have discussions"() {
        when:
        def result = discussionService.getDiscussionStudent(student.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "non-existing student wants to see discussions"() {
        when:
        discussionService.getDiscussionStudent(INVALID_ID)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "user is not a student"() {
        when:
        discussionService.getDiscussionStudent(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_IS_NOT_STUDENT
    }

    def createBasicDiscussion(User student, Question question, DiscussionDto discussionDto) {
        Discussion discussion = new Discussion(student, question, discussionDto)
        MessageDto messageDto = new MessageDto()
        messageDto.setMessage(TEACHER_ANSWER)
        messageDto.setUserName(TEACHER_NAME)
        new Message(discussion, teacher, messageDto)
        discussionRepository.save(discussion)

        student.addDiscussion(discussion)
    }

    @TestConfiguration
    static class DiscussionServiceImplTestContextConfiguration {

        @Bean
        DiscussionService discussionService() {
            return new DiscussionService()
        }

        @Bean
        CourseService courseService() {
            return new CourseService()
        }
    }
}
