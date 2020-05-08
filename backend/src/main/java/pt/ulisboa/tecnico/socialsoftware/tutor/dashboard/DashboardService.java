package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.Tournament;
import pt.ulisboa.tecnico.socialsoftware.tutor.tournament.TournamentService;

import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.StudentQuestionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;

import java.util.*;
import java.util.stream.Collectors;

import java.util.Set;


import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.USER_NOT_FOUND;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseExecutionRepository courseExecutionRepository;

    @Autowired
    private TournamentService tournamentService;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDashDto getTournamentStats(int userId, int executionId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        CourseExecution courseExecution = courseExecutionRepository.findById(executionId)
                .orElseThrow(() -> new TutorException(ErrorMessage.COURSE_EXECUTION_NOT_FOUND, executionId));

        tournamentService.updateTournamentsState(courseExecution.getTournaments());

        TournamentDashDto statsDto = new TournamentDashDto();

        List<ClosedTournamentDto> closedTourns = user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution().getId() == executionId)
                .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.TOURNAMENT))
                .filter(quizAnswer -> quizAnswer.getQuiz().getTournament().getState().equals(Tournament.State.CLOSED))
                .map(quizAnswer -> new ClosedTournamentDto(quizAnswer, calculateRanking(quizAnswer.getQuiz().getTournament(), user)))
                .sorted(Comparator.comparing(ClosedTournamentDto::getConclusionDate).reversed())
                .collect(Collectors.toList());

        int totalTournaments = closedTourns.size();
        int totalSolved = (int) closedTourns.stream().filter(dto -> dto.getRanking() != 0).count();
        int totalUnsolved = totalTournaments - totalSolved;
        int totalFirst = (int) closedTourns.stream().filter(dto -> dto.getRanking() == 1).count();
        int totalSecond = (int) closedTourns.stream().filter(dto -> dto.getRanking() == 2).count();
        int totalThird = (int) closedTourns.stream().filter(dto -> dto.getRanking() == 3).count();

        int totalUnranked = totalSolved - (totalFirst + totalSecond + totalThird);

        List<Option> nonNullOptions = user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution().getId() == executionId)
                .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.TOURNAMENT))
                .filter(quizAnswer -> quizAnswer.getQuiz().getTournament().getState().equals(Tournament.State.CLOSED))
                .map(QuizAnswer::getQuestionAnswers)
                .flatMap(Collection::stream)
                .map(QuestionAnswer::getOption)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int correctAnswers = (int) nonNullOptions.stream()
                .filter(Option::getCorrect)
                .count();

        int wrongAnswers = (int) nonNullOptions.stream()
                .filter(opt -> !opt.getCorrect())
                .count();

        int totalPerfect = (int) user.getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution().getId() == executionId)
                .filter(quizAnswer -> quizAnswer.getQuiz().getType().equals(Quiz.QuizType.TOURNAMENT))
                .filter(quizAnswer -> quizAnswer.getQuiz().getTournament().getState().equals(Tournament.State.CLOSED))
                .filter(QuizAnswer::isCompleted)
                .map(QuizAnswer::getQuestionAnswers)
                .filter(ans -> ans.stream().allMatch(quest -> quest.getOption() != null && quest.getOption().getCorrect()))
                .count();

        int score = Math.max(0, 5 * totalPerfect + 10 * totalFirst + 5 * totalSecond + 3 * totalThird - 10 * totalUnsolved);

        statsDto.setTotalTournaments(totalTournaments);
        statsDto.setTotalSolved(totalSolved);
        statsDto.setTotalUnsolved(totalUnsolved);
        statsDto.setTotalFirstPlace(totalFirst);
        statsDto.setTotalSecondPlace(totalSecond);
        statsDto.setTotalThirdPlace(totalThird);
        statsDto.setTotalUnrankedPlace(totalUnranked);
        statsDto.setTotalCorrectAnswers(correctAnswers);
        statsDto.setTotalWrongAnswers(wrongAnswers);
        statsDto.setTotalPerfect(totalPerfect);
        statsDto.setScore(score);
        statsDto.setClosedTournaments(closedTourns);
        statsDto.setAnonimize(user.isAnonymizeTournamentStats());

        return statsDto;
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void changeTournamentStatsPrivacy(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        user.changeTournamentStatsPrivacy();
    }

    public int calculateRanking(Tournament tourn, User user) {
        if (tourn.getQuiz() == null || !tourn.getState().equals(Tournament.State.CLOSED))
            return -1;

        if (tourn.getQuiz().getQuizAnswers().stream()
                .anyMatch(ans -> ans.getUser().getId().equals(user.getId()) && !ans.isCompleted()))
            return 0;

        int correct = (int) tourn.getQuiz().getQuizAnswers().stream()
                .filter(ans -> ans.getUser().getId().equals(user.getId()))
                .map(QuizAnswer::getQuestionAnswers)
                .flatMap(Collection::stream)
                .map(QuestionAnswer::getOption)
                .filter(Objects::nonNull)
                .filter(Option::getCorrect)
                .count();

        List<Integer> results = new ArrayList<>();
        tourn.getQuiz().getQuizAnswers().forEach(ans -> {
            int num = (int) ans.getQuestionAnswers().stream()
                    .map(QuestionAnswer::getOption)
                    .filter(Objects::nonNull)
                    .filter(Option::getCorrect)
                    .count();
            if (!results.contains(num))
                results.add(num);
        });
        Collections.sort(results);
        Collections.reverse(results);

        return results.indexOf(correct) + 1;
    }

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

    public Boolean toggleStudentQuestionStatsVisibility(int userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        return user.toggleStudentQuestionStatsVisibility();
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean canAccessStudentQuestionStats(int userId, int dashboardId) {
        if (dashboardId == userId)
            return true;
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));
        return user.getStudentQuestionStatsVisibility();
    }
}
