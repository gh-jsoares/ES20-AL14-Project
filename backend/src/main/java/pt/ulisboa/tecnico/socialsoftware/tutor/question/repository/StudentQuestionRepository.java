package pt.ulisboa.tecnico.socialsoftware.tutor.question.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.StudentQuestion;

import java.util.Optional;

@Repository
@Transactional
public interface StudentQuestionRepository extends JpaRepository<StudentQuestion, Integer> {

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.title = :title", nativeQuery = true)
    StudentQuestion findStudentQuestionByTitle(String title);

    @Query(value = "SELECT * FROM student_questions sq WHERE sq.key = :key", nativeQuery = true)
    Optional<StudentQuestion> findByKey(Integer key);

    @Query(value = "SELECT MAX(key) FROM student_questions", nativeQuery = true)
    Integer getMaxQuestionNumber();
}