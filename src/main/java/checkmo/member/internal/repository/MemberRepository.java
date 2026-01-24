package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.repository.projection.MemberIdAndNicknameProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String>, MemberRepositoryCustom {

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

    // 생성일시가 특정 시간 이전이고, 추가정보(nickname)가 아직 입력되지 않은(프로필 미완료) 회원 조회
    @Query("SELECT m FROM Member m WHERE m.createdAt < :threshold AND (m.nickName IS NULL OR m.nickName = '')")
    List<Member> findAllGhostMembers(@Param("threshold") LocalDateTime threshold);

    List<Member> findAllByNameAndPhoneNumber(String name, String phoneNumber);
}
