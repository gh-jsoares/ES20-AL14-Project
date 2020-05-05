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
import spock.lang.Unroll

@DataJpaTest
class StudentMakesAdditionalRequestTest extends Specification {
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
    Discussion discussion

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
        message = new MessageDto()
        message.setMessage(TEACHER_ANSWER)
        messages.add(message)
        discussionDto.setMessages(messages)

        teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(teacher)

        courseExecution.addUser(teacher)
        message.setUserName(teacher.getUsername())

        discussion = new Discussion(questionAnswer, student, question, discussionDto)
        discussionRepository.save(discussion)
    }

    def "student answers new question"() {
        given: "a messageDto with an answer from a teacher from the same course execution"
        def messageDto = messageDtoCreation(STUDENT_NAME, MESSAGE)

        when: "adding the answer from the teacher to the discussion"
        discussionService.studentMakesNewQuestion(student.getId(), discussion.getId(), messageDto)

        then: "the data is correct"
        discussionRepository.count() == 1L
        def result = discussionRepository.findAll().get(0)
        result.getMessages().size() == 3
        result.getMessages().get(2).getMessage() == TEACHER_ANSWER
        result.getMessages().get(2).getUser() == teacher
        result.getMessages().get(2).getUser().getRole() == User.Role.TEACHER
    }

    @Unroll
    def "make a new question with an empty message is #msg"() {
        given: "the creation of the message"
        def messageDto = messageDtoCreation(STUDENT_NAME, msg)

        when: "create the discussion"
        discussionService.studentMakesNewQuestion(student.getId(), discussion.getId(), messageDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_MESSAGE_EMPTY

        where:
        msg << [null, "\n\n\n", "\t", "", "    "]
    }

    def "new question with invalid user"() {
        given: "the creation of the message"
        def messageDto = messageDtoCreation(STUDENT_NAME, MESSAGE)

        when:
        discussionService.studentMakesNewQuestion(INVALID_ID, discussion.getId(), messageDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_IS_NOT_STUDENT
    }

    def "new question with invalid user type"() {
        given: "the creation of the message"
        def messageDto = messageDtoCreation(STUDENT_NAME, MESSAGE)

        when:
        discussionService.studentMakesNewQuestion(teacher.getId(), discussion.getId(), messageDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_IS_NOT_STUDENT
    }

    def "new question with invalid student"() {
        given: "the creation of the message"
        def messageDto = messageDtoCreation(STUDENT_NAME, MESSAGE)
        User student2 = new User('student2', STUDENT_NAME + "2", 2, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        when:
        discussionService.studentMakesNewQuestion(student2.getId(), discussion.getId(), messageDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.INVALID_STUDENT
    }

    def messageDtoCreation(String name, String message) {
        def messageDto = new MessageDto()
        messageDto.setUserName(name)
        messageDto.setMessage(message)
        return messageDto
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
