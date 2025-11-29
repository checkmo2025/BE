package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.repository.projection.MemberIdAndNicknameProjection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String> {

    boolean existsByNickName(String nickName);

    Optional<Member> findByNickName(String nickName);

    @Query("select m.id from Member m where m.nickName = :nickName")
    Optional<String> findIdByNickName(@Param("nickName") String nickName);

    @Query("select m.nickName from Member m where m.id = :memberId")
    Optional<String> findNicknameById(@Param("memberId") String memberId);

    @Query("select m.id as id, m.nickName as nickName from Member m where m.id in :memberIds")
    List<MemberIdAndNicknameProjection> findIdAndNicknameByIdIn(@Param("memberIds") List<String> memberIds);

    @Query("select m.id as id, m.nickName as nickName, m.imgUrl as imgUrl from Member m where m.id in :memberIds")
    List<MemberBasicInfoProjection> findIdNicknameAndImgUrlByIdIn(@Param("memberIds") List<String> memberIds);
}
