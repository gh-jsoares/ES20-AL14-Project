package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
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

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

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
        User student = getUser(studentId);

        Question question = getQuestion(questionId);

        checkDuplicates(studentId, questionId);

        Discussion discussion = new Discussion(student, question, discussionDto);
        this.entityManager.persist(discussion);
        return new DiscussionDto(discussion);
    }

    private User getUser(Integer studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, studentId));
        if (student.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.USER_NOT_STUDENT, studentId);
        }
        return student;
    }

    private Question getQuestion(Integer questionId) {
        return questionRepository.findById(questionId).orElseThrow(() -> new TutorException(ErrorMessage.QUESTION_NOT_FOUND, questionId));
    }

    private void checkDuplicates(Integer studentId, Integer questionId) {
        if (!discussionRepository.findDiscussions(studentId, questionId).isEmpty()) {
            throw new TutorException(ErrorMessage.DUPLICATE_DISCUSSION);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void teacherAnswersStudent(Integer discussionId, DiscussionDto discussionDto) {
        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(() -> new TutorException(ErrorMessage.DISCUSSION_NOT_FOUND, discussionId));
        User teacher = checkIfTeacherExists(discussionDto);

        discussion.updateTeacherAnswer(teacher, discussionDto);
    }

    private User checkIfTeacherExists(DiscussionDto discussionDto) {
        User teacher = userRepository.findByUsername(discussionDto.getUserName());
        if (teacher == null) {
            throw new TutorException(ErrorMessage.USER_NOT_FOUND);
        }
        else if (teacher.getRole() != User.Role.TEACHER) {
            throw new TutorException(ErrorMessage.USER_IS_NOT_TEACHER, discussionDto.getUserName());
        }
        return teacher;
    }
}
