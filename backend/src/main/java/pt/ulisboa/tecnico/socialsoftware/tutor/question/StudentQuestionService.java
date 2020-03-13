package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class StudentQuestionService {

    @Autowired
    private StudentQuestionRepository studentQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TopicRepository topicRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto createStudentQuestion(String username, StudentQuestionDto studentQuestionDto) {
        User user = getUserIfExists(username);

        checkDuplicateQuestion(studentQuestionDto);
        generateNextStudentQuestionKey(studentQuestionDto);

        StudentQuestion studentQuestion = new StudentQuestion(user, studentQuestionDto);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto addTopicToStudentQuestion(StudentQuestionDto studentQuestionDto, TopicDto topicDto) {
        Topic topic = getTopicIfExists(topicDto);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionDto);

        studentQuestion.addTopic(topic);
        topic.addStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto removeTopicFromStudentQuestion(StudentQuestionDto studentQuestionDto, TopicDto topicDto) {
        Topic topic = getTopicIfExists(topicDto);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionDto);

        studentQuestion.removeTopic(topic);
        topic.removeStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> listStudentQuestions(String username) {
        User user = getUserIfExists(username);

        checkUserIsStudent(user);

        return studentQuestionRepository.findAll().stream()
                .map(StudentQuestionDto::new)
                .filter(sq -> sq.getCreatorUsername().equals(username))
                .sorted(Comparator
                        .comparing(StudentQuestionDto::getCreationDateAsObject).reversed()
                        .thenComparing(StudentQuestionDto::getTitle))
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto getStudentQuestion(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        checkUserIsStudent(user);
        checkStudentIsCreatorOfQuestion(user, studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> listAllStudentQuestions(String username) {
        User user = getUserIfExists(username);

        checkUserIsTeacher(user);

        return studentQuestionRepository.findAll().stream()
                .map(StudentQuestionDto::new)
                .sorted(Comparator
                        .comparing(StudentQuestionDto::getCreationDateAsObject).reversed()
                        .thenComparing(StudentQuestionDto::getTitle))
                .collect(Collectors.toList());
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto getStudentQuestionAsTeacher(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        checkUserIsTeacher(user);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto approveStudentQuestion(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        checkUserIsTeacher(user);
        studentQuestion.doApprove(user);

        return new StudentQuestionDto(studentQuestion);
    }

    private void checkStudentIsCreatorOfQuestion(User user, StudentQuestion studentQuestion) {
        if (!studentQuestion.getStudent().getUsername().equals(user.getUsername()))
            throw new TutorException(STUDENT_QUESTION_STUDENT_NOT_CREATOR, studentQuestion.getTitle());
    }

    private User getUserIfExists(String username) {
        User user = userRepository.findByUsername(username);

        checkUserExists(user);
        return user;
    }

    private void checkUserIsStudent(User user) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(STUDENT_QUESTION_NOT_A_STUDENT);
    }

    private void checkUserIsTeacher(User user) {
        if (user.getRole() != User.Role.TEACHER)
            throw new TutorException(STUDENT_QUESTION_NOT_A_TEACHER);
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

    private Topic getTopicIfExists(TopicDto topicDto) {
        if (topicDto != null) {
            Optional<Topic> topic = topicRepository.findById(topicDto.getId());
            if(topic.isPresent())
                return topic.get();
        }
        throw new TutorException(STUDENT_QUESTION_TOPIC_NOT_FOUND);
    }

    private StudentQuestion getStudentQuestionIfExists(StudentQuestionDto studentQuestionDto) {
        if (studentQuestionDto != null) {
            Optional<StudentQuestion> studentQuestion = studentQuestionRepository.findById(studentQuestionDto.getId());
            if(studentQuestion.isPresent())
                return studentQuestion.get();
        }
        throw new TutorException(STUDENT_QUESTION_NOT_FOUND);
    }

    private StudentQuestion getStudentQuestionIfExists(int studentQuestionId) {
        StudentQuestionDto studentQuestionDto = new StudentQuestionDto();
        studentQuestionDto.setId(studentQuestionId);
        return getStudentQuestionIfExists(studentQuestionDto);
    }
}