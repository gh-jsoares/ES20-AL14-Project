package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


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

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public CourseDto findTournamentCourseExecution(int tournamentId) {
        return this.tournamentRepository.findById(tournamentId)
                .map(Tournament::getCourseExecution)
                .map(CourseDto::new)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto  tournamentEnrollStudent(int tournamentId, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        Tournament tournament = getTournament(tournamentId);
        tournament.addEnrolledStudent(user);
        user.addEnrolledTournament(tournament);
        return new TournamentDto(tournament, user.getId());
    }

    private Tournament getTournament(int tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournamentId));
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto createTournament(int executionId, TournamentDto tournDto, int userId) {
        CourseExecution courseExecution = getCourseExecution(executionId);
        User user = getUser(userId);
        checkTournamentDto(tournDto);

        Tournament tourn = buildTournament(tournDto, user, courseExecution);
        entityManager.persist(tourn);

        return new TournamentDto(tourn);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelTournament(int tournamentId, int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));

        Tournament tournament = getTournament(tournamentId);

        tournament.cancel(user);
        tournamentRepository.delete(tournament);
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
        return userRepository.findById(userId)
                .orElseThrow(() -> new TutorException(ErrorMessage.USER_NOT_FOUND, userId));
    }

    private void setTopics(Tournament tourn, List<TopicDto> topics) {
        if (topics == null || topics.isEmpty()) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_CONSISTENT, "Topics");
        }

        topics.forEach(topicDto -> tourn.addTopic(topicRepository.findById(topicDto.getId())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId()))));
    }

    private Tournament buildTournament(TournamentDto tournDto, User user, CourseExecution courseExecution) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        tournDto.setCreationDate(LocalDateTime.now().format(formatter));

        Tournament tourn = new Tournament(tournDto);
        tourn.setState(Tournament.State.ENROLL);
        tourn.setCourseExecution(courseExecution);

        try {
            tourn.setCreator(user);
            setTopics(tourn, tournDto.getTopics());
        } catch (TutorException e) {
            tourn.remove();
            throw e;
        }

        return tourn;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<TournamentDto> getOpenTournaments(int executionId, int userId) {
        CourseExecution courseExecution = getCourseExecution(executionId);

        return courseExecution.getTournaments().stream()
                .filter(tourn -> !tourn.getState().equals(Tournament.State.CLOSED))
                .sorted(Comparator.comparing(Tournament::getId).reversed())
                .map(tournament -> new TournamentDto(tournament, userId))
                .collect(Collectors.toList());
    }
}
