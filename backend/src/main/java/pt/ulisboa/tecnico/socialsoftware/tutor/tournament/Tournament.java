package pt.ulisboa.tecnico.socialsoftware.tutor.tournament;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
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
    @JoinColumn(name = "creator_id")
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
        setCreationDate(DateHandler.toLocalDateTime(tournamentDto.getCreationDate()));
        setAvailableDate(DateHandler.toLocalDateTime(tournamentDto.getAvailableDate()));
        setConclusionDate(DateHandler.toLocalDateTime(tournamentDto.getConclusionDate()));
        setSeries(tournamentDto.getSeries());
        setVersion(tournamentDto.getVersion());
    }

    public Integer getId() {
        return id;
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
        checkTitle(title);
        this.title = title.trim();
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
        checkAvailableDate(availableDate);
        this.availableDate = availableDate;
    }

    public LocalDateTime getConclusionDate() {
        return conclusionDate;
    }

    public void setConclusionDate(LocalDateTime conclusionDate) {
        checkConclusionDate(conclusionDate);
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
        if (creator.getRole() != User.Role.STUDENT) {
            throw new TutorException(ErrorMessage.TOURNAMENT_USER_IS_NOT_STUDENT, creator.getId());
        }
        creator.addCreatedTournament(this);
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
        if (this.getState() != State.ENROLL)
            throw new TutorException(TOURNAMENT_NOT_OPEN, getId());
        if (!this.enrolledStudents.add(user))
            throw new TutorException(DUPLICATE_USER);
        user.addEnrolledTournament(this);
    }

    public Set<Topic> getTopics() {
        return topics;
    }

    public void addTopic(Topic topic) {
        if (this.courseExecution == null || !topic.getCourse().getId().equals(this.courseExecution.getCourse().getId())) {
            throw new TutorException(ErrorMessage.TOURNAMENT_TOPIC_WRONG_COURSE, topic.getId());
        }
        if (!this.topics.add(topic)) {
            throw new TutorException(ErrorMessage.DUPLICATE_TOPIC, topic.getName());
        }
        topic.addTournament(this);
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        quiz.setTournament(this);
        this.quiz = quiz;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        checkNumberOfQuestions(numberOfQuestions);
        this.numberOfQuestions = numberOfQuestions;
    }

    public CourseExecution getCourseExecution() {
        return courseExecution;
    }

    public void setCourseExecution(CourseExecution courseExecution) {
        courseExecution.addTournament(this);
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


    private void checkTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Title");
        }
    }

    private void checkNumberOfQuestions(Integer numberOfQuestions) {
        if (numberOfQuestions == null || numberOfQuestions <= 0) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Number of questions");
        }
    }

    private void checkAvailableDate(LocalDateTime availableDate) {
        if (availableDate == null || conclusionDate != null && conclusionDate.isBefore(availableDate) ||
                creationDate != null && availableDate.isBefore(creationDate)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Available date");
        }
    }

    private void checkConclusionDate(LocalDateTime conclusionDate) {
        if (conclusionDate == null || availableDate != null && conclusionDate.isBefore(availableDate) ||
                creationDate != null && conclusionDate.isBefore(creationDate)) {
            throw new TutorException(TOURNAMENT_NOT_CONSISTENT, "Conclusion date");
        }
    }

    public boolean isStudentEnrolled(int id){
        return this.enrolledStudents.stream()
                    .anyMatch(user -> user.getId() == id);
    }

    public void remove() {
        if (this.quiz != null) {
            this.quiz.remove();
            this.quiz = null;
        }

       if (this.creator != null) {
            this.creator.getCreatedTournaments().remove(this);
            this.creator = null;
        }

        if (this.courseExecution != null) {
            this.courseExecution.getTournaments().remove(this);
            this.courseExecution = null;
        }

        this.enrolledStudents.forEach(student -> student.getEnrolledTournaments().remove(this));
        this.enrolledStudents.clear();

        this.topics.forEach(topic -> topic.getTournaments().remove(this));
        this.topics.clear();

    }

    public void cancel(User user) {
        if (user.getRole() != User.Role.STUDENT)
            throw new TutorException(TOURNAMENT_USER_IS_NOT_STUDENT, user.getId());
        if (this.getState() != Tournament.State.ENROLL)
            throw new TutorException(TOURNAMENT_HAS_STARTED, this.getId());
        else if (this.getCreator().getId() != user.getId())
            throw new TutorException(ErrorMessage.TOURNAMENT_USER_IS_NOT_CREATOR, user.getUsername());
        this.remove();
    }
}
