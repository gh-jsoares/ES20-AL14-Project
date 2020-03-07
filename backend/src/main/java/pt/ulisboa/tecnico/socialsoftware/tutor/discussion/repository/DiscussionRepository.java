package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;

import java.util.List;

@Repository
@Transactional
public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    @Query(value = "SELECT * FROM discussions d, questions q, users s, users t " +
            "WHERE d.question_id = q.id AND d.student_id = s.id AND d.teacher_id = t.id" +
            " AND q.id = :questionId AND s.id = :studentId AND t.id = :teacherId", nativeQuery = true)
    List<Discussion> findDiscussions(int studentId, int teacherId, int questionId);
}
