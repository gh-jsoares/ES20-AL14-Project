package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.dto.UserDto;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    public StudentQuestionDto createStudentQuestion(String username, StudentQuestionDto studentQuestionDto) {
        User user = userRepository.findByUsername(username);

        if (studentQuestionRepository.findStudentQuestionByTitle(studentQuestionDto.getTitle()) != null)
            throw new TutorException(DUPLICATE_STUDENT_QUESTION, studentQuestionDto.getTitle());


        if (studentQuestionDto.getKey() == null) {
            int maxQuestionNumber = studentQuestionRepository.getMaxQuestionNumber() != null ?
                    studentQuestionRepository.getMaxQuestionNumber() : 0;
            studentQuestionDto.setKey(maxQuestionNumber + 1);
        }

        StudentQuestion studentQuestion = new StudentQuestion(user, studentQuestionDto);
        studentQuestion.setCreationDate(LocalDateTime.now());
        this.entityManager.persist(studentQuestion);
        return new StudentQuestionDto(studentQuestion);
    }
}