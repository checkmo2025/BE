package checkmo.domain.member.repository;

import checkmo.domain.member.entity.Member;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    @Query("select m.id from Member m where m.nickName = :nickName")
    Optional<String> findIdByNickName(@Param("nickName") String nickName);
}
