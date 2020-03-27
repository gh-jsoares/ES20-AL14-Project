package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DiscussionController {

    @Autowired
    private DiscussionService discussionService;

    @PostMapping("/questions/{questionId}/discussions")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public DiscussionDto createDiscussion(@PathVariable Integer questionId, @Valid @RequestBody DiscussionDto discussionDto) {
        return this.discussionService.createDiscussion(questionId, discussionDto);
    }

    @PostMapping("/discussions/{discussionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#discussionId, 'DISCUSSION.ACCESS')")
    public DiscussionDto teacherAnswersStudent(@PathVariable Integer discussionId, @Valid @RequestBody DiscussionDto discussionDto) {
        return discussionService.teacherAnswersStudent(discussionId, discussionDto);
    }

    @GetMapping("/student/discussions/")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public List<DiscussionDto> getDiscussionsStudent(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return discussionService.getDiscussionStudent(user.getId());
    }

}
