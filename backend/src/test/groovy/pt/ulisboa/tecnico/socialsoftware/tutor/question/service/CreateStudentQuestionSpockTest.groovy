package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import pt.ulisboa.tecnico.socialsoftware.tutor.question.StudentQuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.StudentQuestionDto
import spock.lang.Specification

class CreateStudentQuestionSpockTest extends Specification {

    public static final String COURSE_NAME = "Software Architecture"
    public static final String ACRONYM = "AS1"
    public static final String ACADEMIC_TERM = "1 SEM"
    public static final String QUESTION_TITLE = 'question title'
    public static final String QUESTION_CONTENT = 'question content'
    public static final String OPTION_CONTENT = "optionId content"
    public static final String URL = 'URL'

    def studentQuestionService

    def setup() {
        studentQuestionService = new StudentQuestionService()
    }

    def "create student question with title and 4 options"() {
        given: "a studentQuestionDto"
        def studentQuestionDto = new StudentQuestionDto()
        studentQuestionDto.setKey(1)
        studentQuestionDto.setTitle(QUESTION_TITLE)
        studentQuestionDto.setContent(QUESTION_CONTENT)
        studentQuestionDto.setStatus(Question.Status.AWAITING_APPROVAL.name())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        studentQuestionDto.setOptions(options)

        when:
        studentQuestionService.createStudentQuestion(course.getId(), studentQuestionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_TITLE
        result.getContent() == QUESTION_CONTENT
        result.getImage() == null
        result.getOptions().size() == 1
        result.getCourse().getName() == COURSE_NAME
        course.getQuestions().contains(result)
        def resOption = result.getOptions().get(0)
        resOption.getContent() == OPTION_CONTENT
        resOption.getCorrect()
    }

    def "invalid arguments: title=#title | option1=#option1 | option2=#option2 | option3=#option3 | option4=#option4 | correct-option=#correct-option || errorMessage=#errorMessage"() {
        // an exception is thrown
        expect: false
    }

    def "student question has more than one correct option"() {
        // an exception is thrown
        expect: false
    }

    def "student already created a question with that title"() {
        // an exception is thrown
        expect: false
    }
}