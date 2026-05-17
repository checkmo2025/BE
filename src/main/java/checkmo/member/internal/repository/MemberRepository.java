package checkmo.member.internal.repository;

import checkmo.member.internal.entity.Member;
import checkmo.member.internal.repository.projection.MemberBasicInfoProjection;
import checkmo.member.internal.repository.projection.MemberIdAndNicknameProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, String>, MemberRepositoryCustom {

    boolean existsByNickNameAndDeactivatedAtIsNull(String nickName);
    Optional<Member> findByIdAndDeactivatedAtIsNull(String id);
    List<Member> findAllByIdInAndDeactivatedAtIsNull(List<String> ids);

    @Query("select m from Member m where m.nickName = :nickName and m.deactivatedAt is null")
    Optional<Member> findByNickName(@Param("nickName") String nickName);

    @Query("select m.id from Member m where m.nickName = :nickName and m.deactivatedAt is null")
    Optional<String> findIdByNickName(@Param("nickName") String nickName);

    @Query("select m.nickName from Member m where m.id = :memberId and m.deactivatedAt is null")
    Optional<String> findActiveNicknameById(@Param("memberId") String memberId);

    @Query("select m.id as id, m.nickName as nickName from Member m where m.id in :memberIds and m.deactivatedAt is null")
    List<MemberIdAndNicknameProjection> findActiveIdAndNicknameByIdIn(@Param("memberIds") List<String> memberIds);

    @Query("select m.id as id, m.nickName as nickName, m.imgUrl as imgUrl from Member m where m.id in :memberIds and m.deactivatedAt is null")
    List<MemberBasicInfoProjection> findActiveIdNicknameAndImgUrlByIdIn(@Param("memberIds") List<String> memberIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id in :memberIds and m.deactivatedAt is null order by m.id asc")
    List<Member> lockActiveMembersByIdIn(@Param("memberIds") List<String> memberIds);

    // 생성일시가 특정 시간 이전이고, 추가정보(nickname)가 아직 입력되지 않은(프로필 미완료) 회원 조회
    @Query("SELECT m FROM Member m WHERE m.createdAt < :threshold AND (m.nickName IS NULL OR m.nickName = '')")
    List<Member> findAllGhostMembers(@Param("threshold") LocalDateTime threshold);

    List<Member> findAllByDeactivatedAtBefore(LocalDateTime threshold);

    List<Member> findAllByNameAndPhoneNumber(String name, String phoneNumber);

    @Query("""
            select m.email
            from Member m
            where m.deactivatedAt is null
              and (:keyword is null or lower(m.email) like lower(concat('%', :keyword, '%')))
            order by m.email asc
            """)
    List<String> findActiveEmailsByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByEmail(String email);


    Page<Member> findByIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String idKeyword,
            String emailKeyword,
            Pageable pageable
    );
}
