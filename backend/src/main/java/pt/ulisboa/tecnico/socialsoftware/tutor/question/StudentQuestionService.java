package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
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
        User user = userRepository.findByUsername(username);

        checkUserExists(user);

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

        checkDuplicateTopicInStudentQuestion(studentQuestion, topic);

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

        checkTopicPresentInStudentQuestion(studentQuestion, topic);

        studentQuestion.removeTopic(topic);
        topic.removeStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> listStudentQuestions(String username) {
        User user = userRepository.findByUsername(username);

        checkUserExists(user);

        // TODO: EXTRACT METHOD (ALSO FROM STUDENTQUESTION CONSTRUCTOR MAYBE)
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(STUDENT_QUESTION_NOT_A_STUDENT);

        return studentQuestionRepository.findAll().stream()
                .map(StudentQuestionDto::new)
                .sorted(Comparator
                        .comparing(StudentQuestionDto::getCreationDateAsObject).reversed()
                        .thenComparing(StudentQuestionDto::getTitle))
                .collect(Collectors.toList());
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

    private void checkDuplicateTopicInStudentQuestion(StudentQuestion studentQuestion, Topic topic) {
        if(studentQuestion.getTopics().stream().anyMatch(t -> t.getId().equals(topic.getId()))
                || topic.getStudentQuestions().stream().anyMatch(sq -> sq.getKey().equals(studentQuestion.getKey())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_ALREADY_ADDED);
    }

    private void checkTopicPresentInStudentQuestion(StudentQuestion studentQuestion, Topic topic) {
        if(studentQuestion.getTopics().stream().noneMatch(t -> t.getId().equals(topic.getId()))
                || topic.getStudentQuestions().stream().noneMatch(sq -> sq.getKey().equals(studentQuestion.getKey())))
            throw new TutorException(STUDENT_QUESTION_TOPIC_NOT_PRESENT);
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
            Optional<StudentQuestion> studentQuestion = studentQuestionRepository.findByKey(studentQuestionDto.getKey());
            if(studentQuestion.isPresent())
                return studentQuestion.get();
        }
        throw new TutorException(STUDENT_QUESTION_NOT_FOUND);
    }
}