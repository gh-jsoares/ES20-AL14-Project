package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class DiscussionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
        value = { SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto createDiscussion(Integer studentId, Integer questionId, DiscussionDto discussionDto) {

        String message = discussionDto.getMessageFromStudent();
        if (message == null || message.isBlank() || message.isEmpty()) {
            throw new TutorException(DISCUSSION_MESSAGE_EMPTY);
        }

        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, studentId));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        if (discussionRepository.findDiscussions(studentId, questionId).size() != 0) {
            throw new TutorException(DUPLICATE_DISCUSSION);
        }

        //verifies if student is enrolled in same course execution as question
        List<CourseExecution> studentEnrolledInQuestionCourse = question.getQuizQuestions().stream()
                .map(QuizQuestion::getQuiz)
                .map(Quiz::getCourseExecution)
                .filter(courseExecution -> student.getCourseExecutions().contains(courseExecution))
                .collect(Collectors.toList());

        if (studentEnrolledInQuestionCourse.isEmpty())
            throw new TutorException(USER_NOT_ENROLLED_IN_COURSE, student.getName());

        //TODO:perguntar ao professor se e' preferivel passar pelo quiz ou pelo questionAnswer
        List<Question> answeredQuestions = student.getQuizAnswers().stream()
                .map(QuizAnswer::getQuiz)
                .map(Quiz::getQuizQuestions)
                .flatMap(Collection::stream)
                .map(QuizQuestion::getQuestion)
                .filter(quizQuestion -> quizQuestion.getId().equals(questionId))
                .collect(Collectors.toList());

        if (answeredQuestions.isEmpty()) {
            throw new TutorException(DISCUSSION_QUESTION_NOT_ANSWERED, studentId);
        }

        Discussion discussion = new Discussion(student, question, discussionDto);
        this.entityManager.persist(discussion);
        return new DiscussionDto(discussion);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDto getDiscussionStudent(int discussionId) {
        return discussionRepository.findById(discussionId)
            .map(Discussion::getStudent)
            .map(UserDto::new)
            .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void teacherAnswersStudent(Integer discussionId, DiscussionDto discussionDto) {
        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionId));
        User teacher = userRepository.findByUsername(discussionDto.getUserName());

        //check if the question has been already answered
        if (discussion.getTeacher() != null)
            throw new TutorException(DISCUSSION_ALREADY_ANSWERED);

        //check if the teacher is in the same course execution
        List<CourseExecution> teacherEnrolledInQuestionCourse = discussion.getQuestion().getQuizQuestions().stream()
                .map(QuizQuestion::getQuiz)
                .map(Quiz::getCourseExecution)
                .filter(courseExecution -> teacher.getCourseExecutions().contains(courseExecution))
                .collect(Collectors.toList());

        if (teacherEnrolledInQuestionCourse.isEmpty())
            throw new TutorException(TEACHER_NOT_IN_COURSE_EXECUTION);

        String teacherAnswer = discussionDto.getMessage();
        //check if the answer from the teacher is not null
        if (teacherAnswer == null || teacherAnswer.isBlank() || teacherAnswer.isEmpty()) {
            throw new TutorException(EMPTY_ANSWER);
        }

        discussion.updateTeacherAnswer(teacher, discussionDto);
    }
}
