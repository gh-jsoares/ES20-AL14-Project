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
class TeacherSeesStudentRequestsServiceSpockTest extends Specification {

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

    public static final String MESSAGE = "message"
    public static final String STUDENT_NAME = "student_test"
    public static final String TEACHER_NAME = "teacher_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final String TEACHER_ANSWER = "teacher_answer_test"
    public static final String QUESTION_TITLE = "question_title_test"
    public static final Integer NON_EXISTING_ID = 100;

    Question question
    User student
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

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setCourseExecution(courseExecution)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        courseExecution.addQuiz(quiz)
        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizAnswer = new QuizAnswer(student, quiz)
        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion,  10, null,  0)

        DiscussionDto discussionDto = new DiscussionDto()
        def messages = new ArrayList<MessageDto>()
        def message = new MessageDto()
        message.setMessage(MESSAGE)
        messages.add(message)
        discussionDto.setMessagesDto(messages)

        questionRepository.save(question)
        userRepository.save(student)

        discussion = new Discussion(questionAnswer, student, question, discussionDto)
        discussionRepository.save(discussion)
    }

    def "teacher has one discussion to answer"() {
        given: "a teacher from a course with a discussion to answer"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        when: "searching for teacher course discussions"
        def result = discussionService.getDiscussionTeacher(teacher.getId())

        then: "the returned data is correct"
        result.size() == 1
        result.get(0).getMessages().size() == 1
        result.get(0).getMessages().get(0).getUserName() == student.getUsername()
        result.get(0).getMessages().get(0).getMessage() == MESSAGE
        result.get(0).question.getId() == question.getId()
    }

    def "teacher has no discussions"() {
        given: "a teacher with no courses"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        userRepository.save(teacher)

        when: "search for teacher course discussions"
        def result = discussionService.getDiscussionTeacher(teacher.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "teacher has already answered the discussion"() {
        given: "a teacher from a course with a discussion to answer"
        def teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        and: "an answered discussion"
        def message = new Message()
        message.setUser(teacher)
        message.setMessage(TEACHER_ANSWER)
        discussion.addMessage(message)
        discussion.setNeedsAnswer(false)

        when:
        def result = discussionService.getDiscussionTeacher(teacher.getId())

        then: "the returned data is correct"
        result.size() == 0
    }

    def "get discussion from non-existing teacher"() {
        when:
        discussionService.getDiscussionTeacher(NON_EXISTING_ID)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_FOUND
    }

    def "non-teacher user wants to see discussions "() {
        given: "non-teacher user"
        student

        when:
        discussionService.getDiscussionTeacher(student.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_IS_NOT_TEACHER
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
