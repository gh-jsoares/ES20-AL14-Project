package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionStatsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.MessageDto;
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
    public DiscussionDto createDiscussion(Principal principal, @PathVariable Integer questionId, @Valid @RequestBody DiscussionDto discussionDto) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if (user == null)
            throw new TutorException(AUTHENTICATION_ERROR);
        return this.discussionService.createDiscussion(user.getId(), questionId, discussionDto);
    }

    @PostMapping("/discussions/{discussionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#discussionId, 'DISCUSSION.ACCESS')")
    public DiscussionDto teacherAnswersStudent(Principal principal, @PathVariable Integer discussionId, @Valid @RequestBody MessageDto messageDto) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if (user == null)
            throw new TutorException(AUTHENTICATION_ERROR);
        return discussionService.teacherAnswersStudent(user.getId(), discussionId, messageDto);
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

    @GetMapping("/teacher/discussions/")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<DiscussionDto> getDiscussionsTeacher(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return discussionService.getDiscussionTeacher(user.getId());
    }

    @GetMapping("/questions/{questionId}/{questionAnswerId}/discussions/get")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#questionId, 'QUESTION.ACCESS')")
    public List<DiscussionDto> getDiscussionsQuestion(Principal principal, @PathVariable Integer questionId, @PathVariable Integer questionAnswerId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return discussionService.getDiscussionsQuestion(user.getId(), questionId, questionAnswerId);
    }

    @PostMapping("/discussions/{discussionId}/public")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#discussionId, 'DISCUSSION.ACCESS')")
    public ResponseEntity teacherOpensDiscussionToOtherStudents(Principal principal, @PathVariable Integer discussionId) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        discussionService.openDiscussionToOtherStudents(user.getId(), discussionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/discussions/{discussionId}/message")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#discussionId, 'DISCUSSION.ACCESS')")
    public DiscussionDto studentMakesNewQuestion(Principal principal, @PathVariable Integer discussionId, @Valid @RequestBody MessageDto messageDto) {
        User user = (User) ((Authentication) principal).getPrincipal();
        if (user == null)
            throw new TutorException(AUTHENTICATION_ERROR);
        return discussionService.studentMakesNewQuestion(user.getId(), discussionId, messageDto);
    }

    @GetMapping("/discussions/stats")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public DiscussionStatsDto getDiscussionsStats(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if(user == null){
            throw new TutorException(AUTHENTICATION_ERROR);
        }

        return discussionService.getDiscussionStats(user.getId());
    }
}
