package pt.ulisboa.tecnico.socialsoftware.tutor.question.service.studentquestions


import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification
import spock.lang.Unroll

@DataJpaTest
class ConvertApprovedStudentQuestionToQuestion extends Specification {

    public static final String USER_NAME = "Alfredo Costa"
    public static final String USER_USERNAME = "alcosta"
    public static final String TEACHER_NAME = "Prof Almeida"
    public static final String TEACHER_USERNAME = "prof"
    public static final String QUESTION_TITLE = "question title"
    public static final String QUESTION_CONTENT = "question content"
    public static final String OPTION_CONTENT = "optionId content"
    public static final String COURSE_NAME = "Arquitetura de Software"

    def "convert existing student question to question"() {
        expect: true
    }

    @Unroll
    def "invalid data: studentQuestion=#isStudentQuestion | topic=#isAwaitingApproval || errorMessage=#errorMessage"() {
        expect: true
    }
}
