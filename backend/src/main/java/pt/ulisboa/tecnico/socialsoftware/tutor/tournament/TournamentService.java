package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;

import java.time.LocalDateTime;
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

        for (TopicDto topicDto : topics) {
            if (topicDto.getId() == null)
                throw new TutorException(ErrorMessage.TOPIC_NOT_FOUND, null);
        }

        topics.forEach(topicDto -> tourn.addTopic(topicRepository.findById(topicDto.getId())
                .orElseThrow(() -> new TutorException(ErrorMessage.TOPIC_NOT_FOUND, topicDto.getId()))));
    }

    private Tournament buildTournament(TournamentDto tournDto, User user, CourseExecution courseExecution) {
        tournDto.setCreationDate(DateHandler.toISOString(DateHandler.now()));

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

        LocalDateTime now = DateHandler.now();

        return courseExecution.getTournaments().stream()
                .filter(tourn -> !tourn.getState().equals(Tournament.State.CLOSED))
                .sorted(Comparator.comparing(Tournament::getId).reversed())
                .map(tourn -> {
                    TournamentDto tournDto = new TournamentDto(tourn, userId);
                    if (tourn.getAvailableDate().isBefore(now) &&
                            tourn.getConclusionDate().isAfter(now) &&
                            tourn.isStudentEnrolled(userId)) {
                        tournDto.setStatementQuiz(new StatementQuizDto(getUserQuizAnswer(userId, tourn)));
                    }
                    return tournDto;
                })
                .collect(Collectors.toList());
    }

    private QuizAnswer getUserQuizAnswer(int userId, Tournament tourn){
        return getUser(userId).getQuizAnswers().stream().filter(answer ->
                    answer.getQuiz().getId().equals(tourn.getQuiz().getId()))
                    .findFirst()
                    .orElse(null);
    }
}
