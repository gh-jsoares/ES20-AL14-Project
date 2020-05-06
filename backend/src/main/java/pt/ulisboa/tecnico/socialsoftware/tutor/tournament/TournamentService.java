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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto;
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

    @Autowired
    private StatementService statementService;

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

        courseExecution.getTournaments().forEach(tourn -> {
            if (tourn.getQuiz() == null &&
                    tourn.getEnrolledStudents().size() > 1 &&
                    tourn.getAvailableDate().isBefore(now)) {
                generateTournamentQuiz(tourn.getId());
                tourn.setState(Tournament.State.ONGOING);
            }
            if (!tourn.getState().equals(Tournament.State.CLOSED) &&
                    (tourn.getConclusionDate().isBefore(now) ||
                            tourn.getAvailableDate().isBefore(now) && tourn.getEnrolledStudents().size() <= 1)) {
                tourn.setState(Tournament.State.CLOSED);
            }
        });

        return courseExecution.getTournaments().stream()
                .filter(tourn -> !tourn.getState().equals(Tournament.State.CLOSED))
                .sorted(Comparator.comparing(Tournament::getId).reversed())
                .map(tourn -> {
                    TournamentDto tournDto = new TournamentDto(tourn, userId);
                    if (tourn.getAvailableDate().isBefore(now) &&
                            tourn.getConclusionDate().isAfter(now) &&
                            tourn.isStudentEnrolled(userId) && tourn.getQuiz() != null) {
                        tournDto.setStatementQuiz(getUserQuizAnswer(userId, tourn));
                    }
                    return tournDto;
                })
                .collect(Collectors.toList());
    }

    private StatementQuizDto getUserQuizAnswer(int userId, Tournament tourn) {
        QuizAnswer quizAnswer = getUser(userId).getQuizAnswers().stream().filter(answer ->
                answer.getQuiz().getId().equals(tourn.getQuiz().getId()))
                .findFirst()
                .orElse(null);
        return quizAnswer != null ? new StatementQuizDto(quizAnswer) : null;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void generateTournamentQuiz(int tournId) {
        Tournament tourn = tournamentRepository.findById(tournId)
                .orElseThrow(() -> new TutorException(ErrorMessage.TOURNAMENT_NOT_FOUND, tournId));

        if (tourn.getQuiz() != null)
            throw new TutorException(ErrorMessage.TOURNAMENT_QUIZ_ALREADY_GENERATED);

        if (tourn.getAvailableDate().isAfter(DateHandler.now()))
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_AVAILABLE, tournId);

        if (tourn.getEnrolledStudents().size() <= 1) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_ENOUGH_ENROLLS, tournId);
        }

        TopicConjunction conjunction = createConjunction(tourn);
        Assessment assessment = createAssessment(tourn, conjunction);
        StatementCreationDto quizDetails = createDetails(tourn, assessment);


        CourseExecution execution = tourn.getCourseExecution();
        Quiz quiz;
        try {
            quiz = statementService.generateQuiz(execution, null, quizDetails);
        } catch (TutorException e) {
            assessment.remove();
            execution.getAssessments().remove(assessment);
            entityManager.remove(assessment);
            entityManager.remove(conjunction);
            throw e;
        }

        quiz.setScramble(tourn.isScramble());
        quiz.setType(Quiz.QuizType.TOURNAMENT.toString());
        tourn.setQuiz(quiz);

        tourn.getEnrolledStudents().forEach(user -> {
            QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
            entityManager.persist(quizAnswer);
        });
    }

    private TopicConjunction createConjunction(Tournament tourn) {
        TopicConjunction conjunction = new TopicConjunction();
        tourn.getTopics().forEach(conjunction::addTopic);
        tourn.getTopics().forEach(topic -> topic.addTopicConjunction(conjunction));
        entityManager.persist(conjunction);

        return conjunction;
    }

    private Assessment createAssessment(Tournament tourn, TopicConjunction conjunction) {
        Assessment assessment = new Assessment();
        assessment.setTitle("Tournament " + tourn.getTitle());
        assessment.setStatus(Assessment.Status.DISABLED);
        assessment.setSequence(1);
        assessment.setCourseExecution(tourn.getCourseExecution());
        assessment.addTopicConjunction(conjunction);
        conjunction.setAssessment(assessment);
        entityManager.persist(assessment);

        return assessment;
    }

    private StatementCreationDto createDetails(Tournament tourn, Assessment assessment) {
        StatementCreationDto quizDetails = new StatementCreationDto();
        quizDetails.setNumberOfQuestions(tourn.getNumberOfQuestions());
        quizDetails.setAssessment(assessment.getId());

        return quizDetails;
    }
}
