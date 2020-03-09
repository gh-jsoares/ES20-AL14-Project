package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@Table(name = "tournaments")
public class Tournament {
    public enum State {ENROLL, ONGOING, CLOSED}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private State state = State.CLOSED;

    @Column(nullable = false)
    private String title;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Column(name = "available_date")
    private LocalDateTime availableDate;

    @Column(name = "conclusion_date")
    private LocalDateTime conclusionDate;

    @Column(columnDefinition = "boolean default false")
    private boolean scramble = false;

    @Column(name = "number_of_questions", columnDefinition = "integer default 0")
    private Integer numberOfQuestions = 0;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<User> enrolledStudents = new HashSet<>();

    @ManyToMany(mappedBy = "tournaments")
    private Set<Topic> topics = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="quiz_id")
    private Quiz quiz;

    @ManyToOne
    @JoinColumn(name = "course_execution_id")
    private CourseExecution courseExecution;

    private Integer series;
    private String version;

    public Tournament(){

    }

    public Tournament(TournamentDto tournamentDto){
        setTitle(tournamentDto.getTitle());
        setNumberOfQuestions(tournamentDto.getNumberOfQuestions());
        setState(tournamentDto.getState());
        setScramble(tournamentDto.isScramble());
        setAvailableDate(tournamentDto.getAvailableDate());
        setConclusionDate(tournamentDto.getConclusionDate());
        setSeries(tournamentDto.getSeries());
        setVersion(tournamentDto.getVersion());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(LocalDateTime availableDate) {
        this.availableDate = availableDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        this.conclusionDate = conclusionDate;
    }

    public boolean isScramble() {
        return scramble;
    }

    public void setScramble(boolean scramble) {
        this.scramble = scramble;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Set<User> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void addEnrolledStudent(User user) {
        if (user == null)
            throw new TutorException(USER_IS_NULL);
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(TOURNAMENT_USER_IS_NOT_STUDENT, user.getId());
        if (!user.getCourseExecutions().contains(this.getCourseExecution()))
            throw new TutorException(TOURNAMENT_STUDENT_NOT_ENROLLED_IN_TOURNAMENT_COURSE, user.getId());
        if (getEnrolledStudents().contains(user))
            throw new TutorException(DUPLICATE_USER);
        if (this.getState() != State.ENROLL)
            throw new TutorException(TOURNAMENT_NOT_OPEN, getId());
        this.enrolledStudents.add(user);
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        this.topics.add(topic);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        this.courseExecution = courseExecution;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
