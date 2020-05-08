package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DiscussionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

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
}