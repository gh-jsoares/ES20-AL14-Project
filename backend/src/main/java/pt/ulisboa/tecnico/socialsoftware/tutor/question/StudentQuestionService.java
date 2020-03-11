package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto createStudentQuestion(String username, StudentQuestionDto studentQuestionDto) {
        User user = userRepository.findByUsername(username);

        checkUserExists(user);

        checkDuplicateQuestion(studentQuestionDto);
        generateNextStudentQuestionKey(studentQuestionDto);

        StudentQuestion studentQuestion = new StudentQuestion(user, studentQuestionDto);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    private void checkUserExists(User user) {
        if (user == null)
            throw new TutorException(STUDENT_QUESTION_USER_NOT_FOUND);
    }

    private void generateNextStudentQuestionKey(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto.getKey() == null) {
            int maxQuestionNumber = studentQuestionRepository.getMaxQuestionNumber() != null ?
                    studentQuestionRepository.getMaxQuestionNumber() : 0;
            studentQuestionDto.setKey(maxQuestionNumber + 1);
        }
    }

    private void checkDuplicateQuestion(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionRepository.findStudentQuestionByTitle(studentQuestionDto.getTitle()) != null)
            throw new TutorException(DUPLICATE_STUDENT_QUESTION, studentQuestionDto.getTitle());
    }
}