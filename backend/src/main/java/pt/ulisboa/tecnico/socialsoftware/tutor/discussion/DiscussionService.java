package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

@Service
public class DiscussionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
        value = { SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto createDiscussion(Integer userId, Integer questionId, DiscussionDto discussionDto) {
        User student = getStudentById(userId);

        Question question = getQuestion(questionId);

        QuestionAnswer questionAnswer = getQuestionAnswer(discussionDto.getId());

        checkDuplicates(student.getId(), questionId);

        Discussion discussion = new Discussion(questionAnswer, student, question, discussionDto);
        this.entityManager.persist(discussion);
        return new DiscussionDto(discussion);
    }

    private QuestionAnswer getQuestionAnswer(Integer questionAnswerId) {
        return questionAnswerRepository.findById(questionAnswerId)
                .orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_ANSWER_NOT_FOUND, questionAnswerId));
    }

    private User getStudentById(Integer studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, studentId));
        if (student.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.USER_IS_NOT_STUDENT, studentId);
        }
        return student;
    }

    private User getTeacherById(Integer teacherId) {
        User user = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, teacherId));
        if (user.getRole() != User.Role.TEACHER) {
            throw new TutorException(ErrorMessage.USER_IS_NOT_TEACHER, teacherId);
        }
        return user;
    }

    private Question getQuestion(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, questionId));
    }

    private void checkDuplicates(Integer studentId, Integer questionId) {
        if (!discussionRepository.findDiscussions(studentId, questionId).isEmpty()) {
            throw new TutorException(ErrorMessage.DUPLICATE_DISCUSSION);
        }
    }

    @Retryable(
        value = { SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionDto teacherAnswersStudent(Integer userId, Integer discussionId, DiscussionDto discussionDto) {
        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(() -> new TutorException(ErrorMessage.DISCUSSION_NOT_FOUND, discussionId));
        User teacher = getTeacherById(userId);

        discussion.updateTeacherAnswer(teacher, discussionDto);
        return new DiscussionDto(discussion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void openDiscussionToOtherStudents(Integer userId, Integer discussionId) {
        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(() -> new TutorException(ErrorMessage.DISCUSSION_NOT_FOUND, discussionId));
        User teacher = getTeacherById(userId);

        discussion.openDiscussion(teacher);
    }

    @Retryable(
        value = { SQLException.class },
        backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<DiscussionDto> getDiscussionStudent(Integer studentId) {
        User student = getStudentById(studentId);

        Set<Discussion> discussions = student.getDiscussions();
        return discussions.stream()
                .map(DiscussionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<DiscussionDto> getDiscussionTeacher(Integer teacherId) {
        User teacher = getTeacherById(teacherId);

        List<Discussion> discussions = getDiscussionFromCourses(teacher.getCourseExecutions());
        return discussions.stream()
                .map(DiscussionDto::new)
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findDiscussionCourse(Integer discussionId) {
        return discussionRepository.findById(discussionId)
                .map(Discussion::getQuestion)
                .map(Question::getCourse)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(ErrorMessage.DISCUSSION_NOT_FOUND, discussionId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Discussion> getDiscussionFromCourses(Set<CourseExecution> courses) {
        return courses.stream()
                .map(CourseExecution::getQuizzes)
                .flatMap(Collection::stream)
                .map(Quiz::getQuizQuestions)
                .flatMap(Collection::stream)
                .map(QuizQuestion::getQuestion)
                .map(Question::getDiscussions)
                .flatMap(Collection::stream)
                .distinct()
                .sorted(Comparator.comparing(Discussion::needsAnswer, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
