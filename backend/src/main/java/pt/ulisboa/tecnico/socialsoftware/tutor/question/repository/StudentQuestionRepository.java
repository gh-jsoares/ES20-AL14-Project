package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.course_id = :courseId AND sq.title = :title", nativeQuery = true)
    Optional<StudentQuestion> findStudentQuestionByTitleInCourse(int courseId, String title);

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.key = :key", nativeQuery = true)
    Optional<StudentQuestion> findByKey(Integer key);

    @Query(value = "SELECT MAX(key) FROM student_questions", nativeQuery = true)
    Integer getMaxQuestionNumber();

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.course_id = :courseId", nativeQuery = true)
    List<StudentQuestion> findAllStudentQuestionsInCourse(int courseId);

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.course_id = :courseId AND sq.student_id = :studentId", nativeQuery = true)
    List<StudentQuestion> findStudentQuestionsInCourse(int courseId, int studentId);

    @Query(value = "SELECT count(*) FROM questions q WHERE q.course_id = :courseId AND q.status = 'AVAILABLE'", nativeQuery = true)
    Integer getAvailableQuestionsSize(int courseId);

}