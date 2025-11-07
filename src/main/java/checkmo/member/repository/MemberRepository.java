package checkmo.member.repository;

import checkmo.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    Optional<Member> findByNickName(String nickName);

    @Query("select m.id from Member m where m.nickName = :nickName")
    Optional<String> findIdByNickName(@Param("nickName") String nickName);

    @Query("select m.nickName from Member m where m.id = :memberId")
    Optional<String> findNicknameById(@Param("memberId") String memberId);

    @Query("select m.nickName, m.id from Member m where m.nickName in :nicknames")
    List<Object[]> findNicknameAndIdByNicknameIn(List<String> nicknames);

    @Query("select m.id, m.nickName from Member m where m.id in :memberIds")
    List<Object[]> findIdAndNicknameByIdIn(@Param("memberIds") List<String> memberIds);

    @Query("select m.id, m.nickName, m.imgUrl from Member m where m.id in :memberIds")
    List<Object[]> findIdNicknameAndImgUrlByIdIn(@Param("memberIds") List<String> memberIds);
}
