package pt.ulisboa.tecnico.socialsoftware.tutor.discussion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.DiscussionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.api.TopicController;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class DiscussionController {
    private static Logger logger = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    private DiscussionService discussionService;

    @PutMapping("/discussion/{discussionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#discussionId, 'DISCUSSION.ACESS')")
    public DiscussionDto teacherAnswersStudent(@PathVariable Integer discussionId, @Valid @RequestBody DiscussionDto discussionDto) {
        return discussionService.teacherAnswersStudent(discussionId, discussionDto);
    }

}
