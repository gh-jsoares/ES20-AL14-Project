package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import pt.ulisboa.tecnico.socialsoftware.tutor.course.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseExecutionRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseService
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DiscussionStatsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository
import spock.lang.Specification

@DataJpaTest
class StudentTogglesDiscussionStatsTest extends Specification {

    @Autowired
    DashboardService dashboardService

    @Autowired
    CourseService courseService

    @Autowired
    UserRepository userRepository

    @Autowired
    CourseRepository courseRepository

    @Autowired
    CourseExecutionRepository courseExecutionRepository

    public static final String STUDENT_NAME = "student_test"
    public static final String TEACHER_NAME = "teacher_test"
    public static final String COURSE_NAME = "course_test"
    public static final String COURSE_ACRONYM = "acronym_test"
    public static final String COURSE_ACADEMIC_TERM = "academic_term_test"
    public static final Integer INVALID_ID = -1

    User teacher
    User student
    Course course
    CourseExecution courseExecution

    def setup() {
        CourseDto courseDto = new CourseDto()
        courseDto.setCourseType(Course.Type.TECNICO)
        courseDto.setName(COURSE_NAME)
        courseDto.setAcronym(COURSE_ACRONYM)
        courseDto.setAcademicTerm(COURSE_ACADEMIC_TERM)
        course = new Course(COURSE_NAME, Course.Type.TECNICO)
        courseRepository.save(course)
        courseService.createTecnicoCourseExecution(courseDto)
        courseExecution = courseExecutionRepository.findAll().get(0)

        student = new User('student', STUDENT_NAME, 1, User.Role.STUDENT)
        student.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        teacher = new User('teacher', TEACHER_NAME, 2, User.Role.TEACHER)
        teacher.getCourseExecutions().add(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)
    }

    def "student makes discussion stats public"() {
        given: "a student"
        student
        and: "a boolean"
        when:
        def result = dashboardService.toggleDiscussionStats(student.getId())

        then:
        result.getAreDiscussionsPublic()
        student.getDiscussionsPrivacy()
    }

    def "student makes discussion stats private again"() {
        given: "a student"
        student
        when:
        DiscussionStatsDto result1 = dashboardService.toggleDiscussionStats(student.getId())
        DiscussionStatsDto result2 = dashboardService.toggleDiscussionStats(student.getId())
        then:
        result1.getAreDiscussionsPublic()
        !result2.getAreDiscussionsPublic()
        !student.getDiscussionsPrivacy()
    }

    def "student makes discussion stats public again"() {
        given: "a student"
        student
        when:
        def result1 = dashboardService.toggleDiscussionStats(student.getId())
        def result2 = dashboardService.toggleDiscussionStats(student.getId())
        def result3 = dashboardService.toggleDiscussionStats(student.getId())
        then:
        result1.getAreDiscussionsPublic()
        !result2.getAreDiscussionsPublic()
        result3.getAreDiscussionsPublic()
        student.getDiscussionsPrivacy()
    }

    def "non-student user toggles discussion stats"() {
        given: "a teacher"
        teacher

        when: "teacher wants to see his discussion stats"
        dashboardService.toggleDiscussionStats(teacher.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_IS_NOT_STUDENT
    }

    def "non-existent user toggles discussion stats"() {
        when:
        dashboardService.toggleDiscussionStats(INVALID_ID)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.USER_NOT_FOUND
    }
    @TestConfiguration
    static class DashboardAndCourseServiceImplTestContextConfiguration {

        @Bean
        DashboardService dashboardService() {
            return new DashboardService()
        }

        @Bean
        CourseService courseService() {
            return new CourseService()
        }
    }
}