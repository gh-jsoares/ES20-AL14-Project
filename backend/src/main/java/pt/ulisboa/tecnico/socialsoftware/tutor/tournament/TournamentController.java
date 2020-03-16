package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping("/tournaments/{tournamentId}/enroll")
    @PreAuthorize("hasRole('ROLE_STUDENT') or (hasRole('ROLE_DEMO_STUDENT') and hasPermission(#tournamentId, 'TOURNAMENT.ACCESS'))")
    public TournamentDto createTournamentEnroll(Principal principal, @PathVariable int tournamentId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.tournamentEnrollStudent(tournamentId, user.getId());
    }


    @PostMapping("/executions/{executionId}/tournaments")
    @PreAuthorize("(hasRole('ROLE_STUDENT') or hasRole('ROLE_DEMO_STUDENT')) and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDto createTournament(Principal principal, @PathVariable int executionId, @Valid @RequestBody TournamentDto tournDto) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return tournamentService.createTournament(executionId, tournDto, user.getId());
    }
}