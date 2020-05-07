package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.StudentQuestionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Retryable(
            value = { SQLException.class },
    backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionStatsDto getStudentQuestionStats(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        Set<StudentQuestion> studentQuestions = user.getStudentQuestions();

        long total = studentQuestions.size();
        long approved = studentQuestions.stream().filter(sq -> sq.getStatus() == StudentQuestion.Status.ACCEPTED).count();
        long rejected = studentQuestions.stream().filter(sq -> sq.getStatus() == StudentQuestion.Status.REJECTED).count();

        return new StudentQuestionStatsDto(user, total, approved, rejected);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ResponseEntity toggleStudentQuestionStatsVisibility(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        user.toggleStudentQuestionStatsVisibility();

        return ResponseEntity.ok(200);
    }
}
