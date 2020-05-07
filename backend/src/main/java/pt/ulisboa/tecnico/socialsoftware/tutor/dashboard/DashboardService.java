package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DiscussionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionStatsDto getDiscussionStats(Integer userId) {
        User student = getStudentById(userId);

        return getDiscussionStatsDto(student);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public DiscussionStatsDto toggleDiscussionStats(Integer userId, Boolean bool){
        User student = getStudentById(userId);
        student.setDiscussionsPrivacy(bool);

        return getDiscussionStatsDto(student);
    }

    private DiscussionStatsDto getDiscussionStatsDto(User student) {
        DiscussionStatsDto discussionStatsDto = new DiscussionStatsDto();

        int discussionsNumber = student.getDiscussions().size();

        int publicDiscussionsNumber = (int) student.getDiscussions().stream()
                .filter(Discussion::isVisibleToOtherStudents)
                .count();

        discussionStatsDto.setAreDiscussionsPublic(student.getDiscussionsPrivacy());
        discussionStatsDto.setDiscussionsNumber(discussionsNumber);
        discussionStatsDto.setPublicDiscussionsNumber(publicDiscussionsNumber);
        return discussionStatsDto;
    }

    private User getStudentById(Integer studentId) {
        User student = userRepository.findById(studentId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, studentId));
        if (student.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.USER_IS_NOT_STUDENT, studentId);
        }
        return student;
    }
}