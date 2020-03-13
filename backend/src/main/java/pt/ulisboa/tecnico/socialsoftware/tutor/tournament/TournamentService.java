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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


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
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        Tournament tournament = getTournament(tournamentDto);
        tournament.addEnrolledStudent(user);
        return new TournamentDto(tournament);
    }

    private Tournament getTournament(TournamentDto tournamentDto) {
        checkNotNullTournament(tournamentDto);
        return tournamentRepository.findById(tournamentDto.getId())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentDto.getId()));
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
        CourseExecution courseExecution = getCourseExecution(executionId);

        checkTournamentDto(tournDto);
        User user = getUser(userId);
        checkTopics(tournDto, courseExecution);

        Tournament tourn = createTournament(tournDto, user, courseExecution);

        return new TournamentDto(tourn);
    }

    private void checkTournamentDto(TournamentDto tournDto) {
        if (tournDto == null) {
            throw new TutorException(ErrorMessage.TOURNAMENT_IS_NULL);
        }
    }

    private CourseExecution getCourseExecution(int executionId) {
        return courseExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new TutorException(ErrorMessage.COURSE_EXECUTION_NOT_FOUND, executionId));
    }

    private User getUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        if (user.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT, user.getId());
        }

        return user;
    }

    private void checkTopics(TournamentDto tournDto, CourseExecution courseExecution) {
        if (tournDto.getTopics() == null || tournDto.getTopics().isEmpty()) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_CONSISTENT, "Topics");
        }

        int courseId = courseExecution.getCourse().getId();
        for (TopicDto topicDto : tournDto.getTopics()) {
            Topic topic = topicRepository.findById(topicDto.getId())
                    .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId()));
            if (topic.getCourse().getId() != courseId) {
                throw new TutorException(ErrorMessage.TOURNAMENT_TOPIC_WRONG_COURSE, topicDto.getId());
            }
        }
    }

    private Tournament createTournament(TournamentDto tournDto, User user, CourseExecution courseExecution) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        tournDto.setCreationDate(LocalDateTime.now().format(formatter));

        Tournament tourn = new Tournament(tournDto);
        tourn.setState(Tournament.State.ENROLL);
        tourn.setCourseExecution(courseExecution);
        tourn.setCreationDate(LocalDateTime.now());
        tourn.setCreator(user);

        tournDto.getTopics().forEach(topicDto -> topicRepository.findById(topicDto.getId()).ifPresent(tourn::addTopic));

        entityManager.persist(tourn);
        return tourn;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenTournaments(int executionId) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        return courseExecution.getTournaments().stream()
                .filter(tourn -> !tourn.getState().equals(Tournament.State.CLOSED))
                .sorted(Comparator.comparing(Tournament::getId).reversed())
                .map(TournamentDto::new)
                .collect(Collectors.toList());
    }
}
