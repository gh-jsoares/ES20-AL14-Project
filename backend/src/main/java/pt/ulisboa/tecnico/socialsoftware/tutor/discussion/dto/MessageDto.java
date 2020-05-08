package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Message;

import java.io.Serializable;

public class MessageDto implements Serializable {

    private String message;

    private String userName;

    public MessageDto() {}

    public MessageDto(Message message) {
        setMessage(message.getMessage());
        setUserName(message.getUser().getUsername());
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
