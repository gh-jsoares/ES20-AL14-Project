package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND;


@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto  tournamentEnrollStudent(TournamentDto tournamentDto, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Tournament tournament = getTournament(tournamentDto);
        tournament.addEnrolledStudent(user);
        return new TournamentDto(tournament);
    }

    private Tournament getTournament(TournamentDto tournamentDto) {
        checkNotNullTournament(tournamentDto);
        Tournament tournament = tournamentRepository.findById(tournamentDto.getId()).orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentDto.getId()));
        if (tournament == null) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentDto.getId());
        }
        return tournament;
    }

    private void checkNotNullTournament(TournamentDto tournamentDto) {
        if (tournamentDto.getId() == null)
            throw new TutorException(ErrorMessage.TOURNAMENT_IS_NULL);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int executionId, TournamentDto tournDto, int userId) {

        if (tournDto == null) {
            throw new TutorException(ErrorMessage.TOURNAMENT_IS_NULL);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        if (user == null) {
            throw new TutorException(ErrorMessage.USER_IS_NULL);
        } else if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT, user.getId());
        }

        CourseExecution courseExecution = courseExecutionRepository.findById(executionId).orElseThrow(() -> new TutorException(ErrorMessage.COURSE_EXECUTION_NOT_FOUND, executionId));

        if (tournDto.getTopics() == null || tournDto.getTopics().isEmpty()) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_CONSISTENT, "Topics");
        }
        List<TopicDto> topics = tournDto.getTopics();
        for (TopicDto topicDto : topics) {
            if (topicRepository.findById(topicDto.getId()).isEmpty()) {
                throw new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId());
            }
        }

        Tournament tourn = new Tournament(tournDto);
        tourn.setState(Tournament.State.ENROLL);
        tourn.setCourseExecution(courseExecution);
        tourn.setCreationDate(LocalDateTime.now());

        tourn.setCreator(user);

        for (TopicDto topicDto : topics) {
            Topic topic = topicRepository.findById(topicDto.getId()).get();
            tourn.addTopic(topic);
        }

        entityManager.persist(tourn);

        return new TournamentDto(tourn);
    }

}
