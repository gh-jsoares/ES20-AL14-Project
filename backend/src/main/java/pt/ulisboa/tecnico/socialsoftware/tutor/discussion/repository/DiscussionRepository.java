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
    @Query(value = "SELECT * FROM discussions d WHERE d.id = :id", nativeQuery = true)
    Discussion findDiscussion(int id);

    @Query(value = "SELECT * FROM discussions d WHERE d.question_id = :questionId AND d.student_key = :studentKey AND d.teacher_key = :teacherKey", nativeQuery = true)
    List<Discussion> findDiscussions(int studentKey, int teacherKey, int questionId);
}
