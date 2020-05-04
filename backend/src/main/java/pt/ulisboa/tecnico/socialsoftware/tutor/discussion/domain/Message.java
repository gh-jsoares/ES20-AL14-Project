package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto.MessageDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import javax.persistence.*;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String message;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "discussion_id")
    private Discussion discussion;

    public Message() {}

    public Message(Discussion discussion, User user, MessageDto messageDto) {
        checkMessage(messageDto.getMessage());
        setMessage(messageDto.getMessage());
        setDiscussion(discussion);
        setUser(user);
        discussion.addMessage(this);
        user.addMessage(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public void setDiscussion(Discussion discussion) {
        this.discussion = discussion;
    }

    private void checkMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new TutorException(ErrorMessage.DISCUSSION_MESSAGE_EMPTY);
        }
    }
}
