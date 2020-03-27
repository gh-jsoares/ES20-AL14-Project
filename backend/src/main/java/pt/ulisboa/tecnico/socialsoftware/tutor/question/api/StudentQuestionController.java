package pt.ulisboa.tecnico.socialsoftware.tutor.question.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
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
    public StudentQuestionDto createStudentQuestion(Principal principal, @PathVariable int courseId, @Valid @RequestBody StudentQuestionDto studentQuestion) {
        studentQuestion.setStatus(StudentQuestion.Status.AWAITING_APPROVAL.name());
        return this.studentQuestionService.createStudentQuestion(courseId, getAuthUser(principal).getId(), studentQuestion);
    }

    @PutMapping("/questions/student/{studentQuestionId}/image")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public String uploadImage(Principal principal, @PathVariable Integer studentQuestionId, @RequestParam("file") MultipartFile file) throws IOException {
        logger.debug("uploadImage  studentQuestionId: {}: , filename: {}", studentQuestionId, file.getContentType());

        StudentQuestionDto studentQuestionDto = studentQuestionService.getStudentQuestion(getAuthUser(principal).getId(), studentQuestionId);
        String url = studentQuestionDto.getImage() != null ? studentQuestionDto.getImage().getUrl() : null;
        if (url != null && Files.exists(getTargetLocation(url))) {
            Files.delete(getTargetLocation(url));
        }

        int lastIndex = Objects.requireNonNull(file.getContentType()).lastIndexOf('/');
        String type = file.getContentType().substring(lastIndex + 1);

        studentQuestionService.uploadImage(studentQuestionId, type);

        url = studentQuestionService.getStudentQuestion(getAuthUser(principal).getId(), studentQuestionId).getImage().getUrl();
        Files.copy(file.getInputStream(), getTargetLocation(url), StandardCopyOption.REPLACE_EXISTING);

        return url;
    }

    @PutMapping("/questions/student/{studentQuestionId}/topics")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public ResponseEntity updateQuestionTopics(@PathVariable Integer studentQuestionId, @RequestBody Integer[] topics) {
        studentQuestionService.updateStudentQuestionTopics(studentQuestionId, topics);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses/{courseId}/questions/student/all")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<StudentQuestionDto> getAllStudentQuestionAsTeacher(Principal principal, @PathVariable int courseId){
        return this.studentQuestionService.listAllStudentQuestions(courseId, getAuthUser(principal).getId());
    }

    @GetMapping("/questions/student/all/{studentQuestionId}")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public StudentQuestionDto getStudentQuestionAsTeacher(Principal principal, @PathVariable Integer studentQuestionId) {
        return this.studentQuestionService.getStudentQuestionAsTeacher(getAuthUser(principal).getId(), studentQuestionId);
    }

    @PutMapping("/questions/student/all/{studentQuestionId}/approve")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public ResponseEntity studentQuestionApprove(Principal principal, @PathVariable Integer studentQuestionId) {
        this.studentQuestionService.approveStudentQuestion(getAuthUser(principal).getId(), studentQuestionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/questions/student/all/{studentQuestionId}/reject")
    @PreAuthorize("hasRole('ROLE_TEACHER') and hasPermission(#studentQuestionId, 'STUDENTQUESTION.ACCESS')")
    public ResponseEntity studentQuestionReject(Principal principal, @PathVariable Integer studentQuestionId, @RequestBody String explanation) {
        this.studentQuestionService.rejectStudentQuestion(getAuthUser(principal).getId(), studentQuestionId, explanation);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/courses/{courseId}/questions/student/")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseId, 'COURSE.ACCESS')")
    public List<StudentQuestionDto> getAllStudentQuestionAsStudent(Principal principal, @PathVariable int courseId){
        return this.studentQuestionService.listStudentQuestions(courseId, getAuthUser(principal).getId());
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