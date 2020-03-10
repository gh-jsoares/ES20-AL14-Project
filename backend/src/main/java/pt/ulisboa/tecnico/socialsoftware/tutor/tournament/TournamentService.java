package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_IS_NULL;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.TOURNAMENT_NOT_FOUND;

@Service
public class TournamentService {
    @Autowired
    private TournamentRepository tournamentRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TournamentDto  tournamentEnrollStudent(TournamentDto tournamentDto, User user) {
        Tournament tournament = getTournament(tournamentDto);
        tournament.addEnrolledStudent(user);
        return new TournamentDto(tournament);
    }

    private Tournament getTournament(TournamentDto tournamentDto) {
        checkNotNullTournament(tournamentDto);
        Tournament tournament = tournamentRepository.findById(tournamentDto.getId()).orElseThrow(() -> new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId()));
        if (tournament == null) {
            throw new TutorException(TOURNAMENT_NOT_FOUND, tournamentDto.getId());
        }
        return tournament;
    }

    private void checkNotNullTournament(TournamentDto tournamentDto) {
        if (tournamentDto.getId() == null)
            throw new TutorException(TOURNAMENT_IS_NULL);
    }

    public TournamentDto createTournament(int courseExecutionId, TournamentDto tournDto, User user) {return null;}

}
