package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Message;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DiscussionDto implements Serializable {

    private Integer id;

    private QuestionDto question;

    private boolean visibleToOtherStudents;

    private boolean needsAnswer;

    private List<MessageDto> messages;

    public DiscussionDto() {}

    public DiscussionDto(Discussion discussion) {
        setId(discussion.getId());
        setVisibleToOtherStudents(discussion.isVisibleToOtherStudents());
        setNeedsAnswer(discussion.needsAnswer());
        setQuestion(new QuestionDto(discussion.getQuestion()));
        setMessagesWithDomain(discussion.getMessages());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) { this.id = id; }

    public QuestionDto getQuestion() { return question; }

    public void setQuestion(QuestionDto question) { this.question = question; }

    public List<MessageDto> getMessages() { return messages; }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public void setMessagesWithDomain(List<Message> messages) {
        this.messages = messages.stream()
            .sorted(Comparator.comparingInt(Message::getCounter))
            .map(MessageDto::new)
            .collect(Collectors.toList());
    }

    public boolean isVisibleToOtherStudents() { return visibleToOtherStudents; }

    public void setVisibleToOtherStudents(boolean visibleToOtherStudents) { this.visibleToOtherStudents = visibleToOtherStudents; }

    public boolean isNeedsAnswer() {
        return needsAnswer;
    }

    public void setNeedsAnswer(boolean needsAnswer) {
        this.needsAnswer = needsAnswer;
    }
}
