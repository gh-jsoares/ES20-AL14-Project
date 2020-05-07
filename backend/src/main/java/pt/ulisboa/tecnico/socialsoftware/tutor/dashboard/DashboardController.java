package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/executions/{executionId}/dashboard/tournaments")
    @PreAuthorize("(hasRole('ROLE_STUDENT') or hasRole('ROLE_DEMO_STUDENT')) and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public TournamentDashDto getTournamentStats(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return dashboardService.getTournamentStats(user.getId(), executionId);
    }

    @PostMapping("/executions/{executionId}/dashboard/tournaments/changePrivacy")
    @PreAuthorize("(hasRole('ROLE_STUDENT') or hasRole('ROLE_DEMO_STUDENT')) and hasPermission(#executionId, 'EXECUTION.ACCESS')")
    public ResponseEntity changeTournamentPrivacy(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        dashboardService.changeTournamentStatsPrivacy(user.getId());

        return ResponseEntity.ok().build();
    }

}
