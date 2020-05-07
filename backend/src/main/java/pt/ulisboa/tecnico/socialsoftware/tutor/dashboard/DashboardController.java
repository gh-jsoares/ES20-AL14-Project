package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.StudentQuestionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DashboardController {
    private static Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
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