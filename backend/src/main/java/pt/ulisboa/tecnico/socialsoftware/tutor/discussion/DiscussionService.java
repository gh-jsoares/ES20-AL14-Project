package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
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
import java.util.List;

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
    public DiscussionDto createDiscussion(Integer studentId, Integer teacherId, Integer questionId, DiscussionDto discussionDto) {

        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, studentId));
        User teacher = userRepository.findById(teacherId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, teacherId));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        if (discussionRepository.findDiscussions(studentId, teacherId, questionId) != null) {
            throw new TutorException(DUPLICATE_DISCUSSION, discussionDto.getId());
        }

        Discussion discussion = new Discussion(student, teacher, question, discussionDto);
        this.entityManager.persist(discussion);
        return new DiscussionDto(discussion);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDto findDiscussionTeacher(int discussionId) {
        return discussionRepository.findById(discussionId)
            .map(Discussion::getTeacher)
            .map(UserDto::new)
            .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionId));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public UserDto findDiscussionStudetn(int discussionId) {
        return discussionRepository.findById(discussionId)
            .map(Discussion::getStudent)
            .map(UserDto::new)
            .orElseThrow(() -> new TutorException(DISCUSSION_NOT_FOUND, discussionId));
    }
}
