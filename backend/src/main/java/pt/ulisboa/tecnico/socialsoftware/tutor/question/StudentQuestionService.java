package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.ImageRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.StudentQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ImageRepository imageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto createStudentQuestion(int courseId, String username, StudentQuestionDto studentQuestionDto) {
        User user = getUserIfExists(username);
        Course course = getCourseIfExists(courseId);

        checkDuplicateQuestion(courseId, studentQuestionDto);

        StudentQuestion studentQuestion = new StudentQuestion(course, user, studentQuestionDto);
        studentQuestionRepository.save(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto addTopicToStudentQuestion(int studentQuestionId, int topicId) {
        Topic topic = getTopicIfExists(topicId);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        studentQuestion.addTopic(topic);
        topic.addStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto removeTopicFromStudentQuestion(int studentQuestionId, int topicId) {
        Topic topic = getTopicIfExists(topicId);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        studentQuestion.removeTopic(topic);
        topic.removeStudentQuestion(studentQuestion);
        this.entityManager.persist(studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> listStudentQuestions(int courseId, String username) {
        User user = getUserIfExists(username);
        Course course = getCourseIfExists(courseId);

        checkUserIsStudent(user);

        return studentQuestionRepository.findStudentQuestionsInCourse(course.getId(), user.getId())
                .stream().map(StudentQuestionDto::new)
                .sorted(Comparator
                        .comparing(StudentQuestionDto::getCreationDateAsObject).reversed()
                        .thenComparing(StudentQuestionDto::getTitle))
                .collect(Collectors.toList());
    }

    @Retryable(
            value = {SQLException.class},
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
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StudentQuestionDto> listAllStudentQuestions(int courseId, String username) {
        User user = getUserIfExists(username);
        Course course = getCourseIfExists(courseId);

        checkUserIsTeacher(user);

        return studentQuestionRepository.findAllStudentQuestionsInCourse(course.getId())
                .stream().map(StudentQuestionDto::new)
                .sorted(Comparator
                        .comparing(StudentQuestionDto::getCreationDateAsObject).reversed()
                        .thenComparing(StudentQuestionDto::getTitle))
                .collect(Collectors.toList());
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto getStudentQuestionAsTeacher(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        checkUserIsTeacher(user);
        checkTeacherInStudentQuestionCourse(user, studentQuestion);

        return new StudentQuestionDto(studentQuestion);
    }

    private void checkTeacherInStudentQuestionCourse(User user, StudentQuestion studentQuestion) {
        if (studentQuestion.canTeacherAccess(user))
            throw new TutorException(TEACHER_NOT_IN_COURSE_EXECUTION);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto approveStudentQuestion(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        studentQuestion.doApprove(user);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public StudentQuestionDto rejectStudentQuestion(String username, int studentQuestionId, String explanation) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        studentQuestion.doReject(user, explanation);

        return new StudentQuestionDto(studentQuestion);
    }

    @Retryable(
            value = {SQLException.class},
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void uploadImage(Integer studentQuestionId, String type) {
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);

        Image image = studentQuestion.getImage();

        if (image == null) {
            image = new Image();

            studentQuestion.setImage(image);

            imageRepository.save(image);
        }

        studentQuestion.getImage().setUrl(studentQuestion.getKey() + "." + type);
    }

    public boolean canAccessStudentQuestion(String username, int studentQuestionId) {
        User user = getUserIfExists(username);
        StudentQuestion studentQuestion = getStudentQuestionIfExists(studentQuestionId);
        return !(user.getRole() != User.Role.STUDENT && !studentQuestion.isCreator(user)) && (user.getRole() == User.Role.TEACHER || studentQuestion.canTeacherAccess(user));
    }

    private void checkStudentIsCreatorOfQuestion(User user, StudentQuestion studentQuestion) {
        if (!studentQuestion.isCreator(user))
            throw new TutorException(STUDENT_QUESTION_STUDENT_NOT_CREATOR, studentQuestion.getTitle());
    }

    private User getUserIfExists(String username) {
        User user = userRepository.findByUsername(username);

        checkUserExists(user);
        return user;
    }

    private Course getCourseIfExists(int courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId));
    }

    private void checkUserIsTeacher(User user) {
        if (user.getRole() != User.Role.TEACHER)
            throw new TutorException(STUDENT_QUESTION_NOT_A_TEACHER);
    }

    private void checkUserIsStudent(User user) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(STUDENT_QUESTION_NOT_A_STUDENT);
    }

    private void checkUserExists(User user) {
        if (user == null)
            throw new TutorException(STUDENT_QUESTION_USER_NOT_FOUND);
    }

    private void checkDuplicateQuestion(int courseId, StudentQuestionDto studentQuestionDto) {
        if (studentQuestionRepository.findStudentQuestionByTitleInCourse(courseId, studentQuestionDto.getTitle()).isPresent())
            throw new TutorException(DUPLICATE_STUDENT_QUESTION, studentQuestionDto.getTitle());
    }

    private Topic getTopicIfExists(int topicId) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty())
            throw new TutorException(STUDENT_QUESTION_TOPIC_NOT_FOUND);
        return topic.get();
    }

    private StudentQuestion getStudentQuestionIfExists(int studentQuestionId) {
        Optional<StudentQuestion> studentQuestion = studentQuestionRepository.findById(studentQuestionId);
        if (studentQuestion.isEmpty())
            throw new TutorException(STUDENT_QUESTION_NOT_FOUND);

        return studentQuestion.get();
    }

}