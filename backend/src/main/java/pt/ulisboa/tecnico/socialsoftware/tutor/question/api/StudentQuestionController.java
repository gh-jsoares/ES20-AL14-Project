package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Objects;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.AUTHENTICATION_ERROR;

@RestController
public class StudentQuestionController {
    private static Logger logger = LoggerFactory.getLogger(StudentQuestionController.class);

    private StudentQuestionService studentQuestionService;

    @Value("${figures.dir}")
    private String figuresDir;

    StudentQuestionController(StudentQuestionService studentQuestionService) {
        this.studentQuestionService = studentQuestionService;
    }

    @PostMapping("/courses/{courseId}/questions/student")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public StudentQuestionDto createQuestion(Principal principal, @PathVariable int courseId, @Valid @RequestBody StudentQuestionDto studentQuestion) {
        return this.studentQuestionService.createStudentQuestion(courseId, getAuthUser(principal).getUsername(), studentQuestion);
    }

    @PutMapping("/questions/student/{studentQuestionId}/image")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public String uploadImage(Principal principal, @PathVariable Integer studentQuestionId, @RequestParam("file") MultipartFile file) throws IOException {
        logger.debug("uploadImage  studentQuestionId: {}: , filename: {}", studentQuestionId, file.getContentType());

        StudentQuestionDto studentQuestionDto = studentQuestionService.getStudentQuestion(getAuthUser(principal).getUsername(), studentQuestionId);
        String url = studentQuestionDto.getImage() != null ? studentQuestionDto.getImage().getUrl() : null;
        if (url != null && Files.exists(getTargetLocation(url))) {
            Files.delete(getTargetLocation(url));
        }

        int lastIndex = Objects.requireNonNull(file.getContentType()).lastIndexOf('/');
        String type = file.getContentType().substring(lastIndex + 1);

        studentQuestionService.uploadImage(studentQuestionId, type);

        url = studentQuestionService.getStudentQuestion(getAuthUser(principal).getUsername(), studentQuestionId).getImage().getUrl();
        Files.copy(file.getInputStream(), getTargetLocation(url), StandardCopyOption.REPLACE_EXISTING);

        return url;
    }

    private Path getTargetLocation(String url) {
        String fileLocation = figuresDir + url;
        return Paths.get(fileLocation);
    }

    private User getAuthUser(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();

        if (user == null)
            throw new TutorException(AUTHENTICATION_ERROR);

        return user;
    }

}