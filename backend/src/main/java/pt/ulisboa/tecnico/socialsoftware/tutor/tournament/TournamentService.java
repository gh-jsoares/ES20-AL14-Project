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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.TopicConjunction;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.StatementService;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementCreationDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
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
    private QuestionRepository questionRepository;

    @Autowired
    private QuizService quizService;

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

        courseExecution.getTournaments().forEach(tourn -> {
            LocalDateTime now = DateHandler.now();
            if (tourn.getQuiz() == null &&
                    tourn.getEnrolledStudents().size() > 1 &&
                    tourn.getAvailableDate().isBefore(now)) {
                generateTournamentQuiz(tourn.getId());
                tourn.setState(Tournament.State.ONGOING);
            }
            if (!tourn.getState().equals(Tournament.State.CLOSED) &&
                    (tourn.getConclusionDate().isBefore(now) || tourn.getEnrolledStudents().size() <= 1)) {
                tourn.setState(Tournament.State.CLOSED);
            }
        });

        return courseExecution.getTournaments().stream()
                .filter(tourn -> !tourn.getState().equals(Tournament.State.CLOSED))
                .sorted(Comparator.comparing(Tournament::getId).reversed())
                .map(tournament -> new TournamentDto(tournament, userId))
                .collect(Collectors.toList());
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

        LocalDateTime now = DateHandler.now();

        if (tourn.getAvailableDate().isAfter(now))
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_AVAILABLE, tournId);

        /*if (tourn.getConclusionDate().isBefore(now))
            throw new TutorException(ErrorMessage.TOURNAMENT_IS_CLOSED, tournId);*/

        if (tourn.getEnrolledStudents().size() <= 1) {
            throw new TutorException(ErrorMessage.TOURNAMENT_NOT_ENOUGH_ENROLLS, tournId);
        }

        // create topic conjunction
        TopicConjunction conjunction = new TopicConjunction();
        tourn.getTopics().forEach(conjunction::addTopic);
        tourn.getTopics().forEach(topic -> topic.addTopicConjunction(conjunction));

        // create assessment
        Assessment assessment = new Assessment();
        assessment.setTitle("Tournament " + tourn.getTitle());
        assessment.setStatus(Assessment.Status.DISABLED);
        assessment.setSequence(1);
        assessment.setCourseExecution(tourn.getCourseExecution());
        assessment.addTopicConjunction(conjunction);
        conjunction.setAssessment(assessment);

        entityManager.persist(conjunction);
        entityManager.persist(assessment);

        // create creation details
        StatementCreationDto quizDetails = new StatementCreationDto();
        quizDetails.setNumberOfQuestions(tourn.getNumberOfQuestions());
        quizDetails.setAssessment(assessment.getId());

        // create quiz
        Quiz quiz = new Quiz();
        quiz.setKey(quizService.getMaxQuizKey() + 1);
        quiz.setCreationDate(DateHandler.now());
        quiz.setScramble(tourn.isScramble());


        /*quiz.setAvailableDate(tourn.getAvailableDate());
        if (tourn.getConclusionDate().isAfter(DateHandler.now())) {
            quiz.setConclusionDate(tourn.getConclusionDate());
        }*/


        CourseExecution courseExecution = tourn.getCourseExecution();


        List<Question> availableQuestions = questionRepository.findAvailableQuestions(courseExecution.getCourse().getId());

        if (quizDetails.getAssessment() != null) {
            availableQuestions = statementService.filterByAssessment(availableQuestions, quizDetails);
        }

        if (availableQuestions.size() < quizDetails.getNumberOfQuestions()) {
            assessment.remove();
            courseExecution.getAssessments().remove(assessment);
            entityManager.remove(assessment);
            entityManager.remove(conjunction);
            throw new TutorException(ErrorMessage.NOT_ENOUGH_QUESTIONS);
        }

        Random rand = new Random(System.currentTimeMillis());
        List<Question> limitedQuestions = new ArrayList<>();
        while (limitedQuestions.size() < quizDetails.getNumberOfQuestions()) {
            int next = rand.nextInt(availableQuestions.size());
            if (!limitedQuestions.contains(availableQuestions.get(next))) {
                limitedQuestions.add(availableQuestions.get(next));
            }
        }
        availableQuestions = limitedQuestions;

        quiz.generate(availableQuestions);


        quiz.setCourseExecution(courseExecution);

        //quiz.setResultsDate(tourn.getConclusionDate());
        quiz.setType(Quiz.QuizType.TOURNAMENT.toString());
        tourn.setQuiz(quiz);

        tourn.getEnrolledStudents().forEach(user -> {
            QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
            entityManager.persist(quizAnswer);
        });

        entityManager.persist(quiz);
    }
}
