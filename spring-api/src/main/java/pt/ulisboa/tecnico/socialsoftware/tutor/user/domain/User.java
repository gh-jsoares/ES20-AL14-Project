package pt.ulisboa.tecnico.socialsoftware.tutor.user.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "Users")
@Table(name = "users")
public class User implements UserDetails {
    public enum Role {STUDENT, TEACHER};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;
    private String name;
    private Integer year;
    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
    Set<QuizAnswer> quizAnswers;

    @Transient
    private Collection<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();


    public User() {
    }

    public User(String name, String username, Role role) {
        this.name = name;
        this.username = username;
        this.role = role.name();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

//    public Set<QuizAnswer> getQuizAnswers() {
//        return quizAnswers;
//    }
//
//    public void setQuizAnswers(Set<QuizAnswer> quizAnswers) {
//        this.quizAnswers = quizAnswers;
//    }
//
//    public void addQuizAnswer(QuizAnswer quizAnswer) {
//        if (quizAnswers == null) {
//            quizAnswers = new HashSet<>();
//        }
//        this.quizAnswers.add(quizAnswer);
//    }


    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean hasRole(String role) {
        return getAuthorities().stream().filter(a -> a.getAuthority().equals(role)).findFirst().isPresent();
    }
}