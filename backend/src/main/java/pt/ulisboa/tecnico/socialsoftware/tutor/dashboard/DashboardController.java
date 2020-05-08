package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DiscussionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.StudentQuestionStatsDto;

import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.TournamentDashDto;
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

    @GetMapping("/discussions/stats")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DiscussionStatsDto getDiscussionsStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return dashboardService.getDiscussionStats(user.getId());
    }

    @GetMapping("/discussions/stats/toggle")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DiscussionStatsDto toggleDiscussionStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return dashboardService.toggleDiscussionStats(user.getId());
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
    public ResponseEntity changeTournamentStatsPrivacy(Principal principal, @PathVariable int executionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        dashboardService.changeTournamentStatsPrivacy(user.getId());

        return ResponseEntity.ok().build();
    }


    @GetMapping("/dashboard/{userId}/questions/student")
    @PreAuthorize("hasPermission(#userId, 'DASHBOARD.STUDENTQUESTION.ACCESS')")
    public StudentQuestionStatsDto getStudentQuestionStatsForUser(@PathVariable int userId) {
        return this.dashboardService.getStudentQuestionStats(userId);
    }

    @GetMapping("/dashboard/questions/student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public StudentQuestionStatsDto getStudentQuestionStats(Principal principal) {
        return this.dashboardService.getStudentQuestionStats(getAuthUser(principal).getId());
    }

    @PutMapping("/dashboard/questions/student")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public Boolean toggleStudentQuestionStatsVisibility(Principal principal) {
        return this.dashboardService.toggleStudentQuestionStatsVisibility(getAuthUser(principal).getId());
    }

    private User getAuthUser(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null)
            throw new TutorException(AUTHENTICATION_ERROR);

        return user;
    }

}