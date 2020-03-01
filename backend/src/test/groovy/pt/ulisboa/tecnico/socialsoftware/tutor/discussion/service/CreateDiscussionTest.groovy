package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
//TODO:import needed data from discussion
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository
import spock.lang.Specification

@DataJpaTest
class CreateDiscussionTest extends Specification {

    @Autowired
    DiscussionService discussionService

    @Autowired
    QuestionRepository questionRepository

    @Autowired
    UserRepository userRepository

    public static final String MESSAGE = "message"

    def question
    def student
    def teacher

    def setup() {
        question = new Question()
        question.setId(1)
        student = new User()
        student.setKey(1)
        teacher = new User()
        teacher.setKey(2)
        questionRepository.save(question)
        userRepository.save(student)
        userRepository.save(teacher)
    }

    def "create a discussion"() {
        given: "the definition of the discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setMessage(MESSAGE)

        when: "create discussion in the repository"
        discussionService.createDiscussion(student.getId(), teacher.getId(), question.getId(), discussionDto)

        then: "insert discussion in the repository"
        discussionRepository.findAll().size() == 1
        def result = discussionRepository.findAll().get(0)
        result.getId() != null
        result.getStudent() != null
        result.getStudent().getKey() != student.getKey()
        result.getTeacher() != null
        result.getTeacher().getKey() != teacher.getKey()
        result.getQuestion() != null
        result.getQuestion().getId() != question.getId()
        /*after inserting discussion in teacher and student profile, test if it is there and confirm student, teacher and question existence*/
    }

    def "create a discussion with the same student, professor and question"() {
        given: "the definition of the discussion"
        def discussion = new Discussion()
        discussion.setQuestion(question)
        discussion.setStudent(student)
        discussion.setTeacher(teacher)
        discussionRepository.save(discussion)
        and: "the creation of the DiscussionDto"
        def discussionDto = new DiscussionDto()

        when: "create another discussion with the same student, professor and question"
        discussionService.createDiscussion(student.getId(), teacher.getId(), question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DUPLICATE_DISCUSSION
    }

    def "create a discussion with an empty message is #msg"() {
        given: "the definition of the discussion"
        def discussionDto = new DiscussionDto()
        discussionDto.setStudentMessage(msg)

        when: "add the discussion"
        discussionService.createDiscussion(student.getId(), teacher.getId(), question.getId(), discussionDto)

        then: "an error occurs"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.DISCUSSION_MESSAGE_EMPTY

        where:
        msg << [null, "\n", "\t"]
    }

    //def "create a discussion with a non existing user/question"() {
    //maybe it can be included in the first test
    //}

    //@TestConfiguration
    //static class DiscussionServiceImplTestContextConfiguration {

    //TODO: probably other services missing
    //    @Bean
    //    DiscussionService discussionService() {
    //        return new DiscussionService()
    //    }
    //}
}
