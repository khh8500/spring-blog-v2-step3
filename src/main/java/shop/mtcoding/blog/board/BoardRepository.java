package shop.mtcoding.blog.board;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import shop.mtcoding.blog.user.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class BoardRepository {
    private final EntityManager em;

    // update board_tb set title = ? where id = ?
    // update board_tb set content = ? where id = ?
    // update board_tb set title = ?, content = ? where id = ?
    @Transactional
    public void updateById(int id, String title, String content){
        Board board = findById(id);
        board.setTitle(title);
        board.setContent(content);
    } // 더티체킹

    @Transactional
    public void deleteById(int id){
        Query query = em.createQuery("delete from Board b where b.id = :id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

    @Transactional
    public Board save(Board board){
        em.persist(board);
        return board;
    }

    public List<Board> findAllV3() {
        String q1 = "select b from Board b order by b.id desc";
        List<Board> boardList = em.createQuery(q1, Board.class).getResultList();

        int[] userIds = boardList.stream().mapToInt(board -> board.getUser().getId()).distinct().toArray();

        String q2 = "select u from User u where u.id in (";
        for (int i = 0; i < userIds.length; i++) {
            if (i == userIds.length - 1) {
                q2 = q2 + userIds[i] + ")";
            } else {
                q2 = q2 + userIds[i] + ",";
            }
        }
        List<User> userList = em.createQuery(q2, User.class).getResultList();

        for (Board board : boardList) {
            for (User user : userList) {
                if (user.getId() == board.getUser().getId()) {
                    board.setUser(user);
                }
            }
        }

        return boardList; // user가 채워져 있어야함.
    }

    public List<Board> findAllV2() {
        Query q1 = em.createQuery("select b from Board b order by b.id desc", Board.class);
        List<Board> boardList = q1.getResultList();

        Set<Integer> userIds = new HashSet<>(); // 중복 제거
        for (Board board : boardList) {
            userIds.add(board.getUser().getId());
        }

        Query q2 = em.createQuery("select u from User u where u.id in :userIds", User.class);
        q2.setParameter("userIds", userIds);
        List<User> userList = q2.getResultList();

        for (Board board : boardList) {
            for (User user : userList) {
                if (user.getId() == board.getUser().getId()) {
                }
            }
        }

        return boardList; // user가 채워져 있어야 함
    }

    public List<Board> findAll() {
        Query query = em.createQuery("select b from Board b order by b.id desc", Board.class);
        return query.getResultList();
    }

    public Board findByIdJoinUser(int id) {
        Query query = em.createQuery("select b from Board b join fetch b.user u where b.id = :id", Board.class); // b에 연관된 것까지 프로젝션
        query.setParameter("id", id);
        return (Board) query.getSingleResult();
    }

    public Board findById(int id) {
        // id, title, content, user_id(이질감), created_at
        Board board = em.find(Board.class, id);
        return board;
    }
}
