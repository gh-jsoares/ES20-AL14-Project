package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Message;

@Repository
@Transactional
public interface MessageRepository extends JpaRepository<Message, Integer> {

}
