package checkmo.member.internal.repository;

import checkmo.member.internal.entity.MemberTerms;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberTermsRepository extends JpaRepository<MemberTerms, Long> {

    @Query("""
            select mt
            from MemberTerms mt
            join fetch mt.terms t
            where mt.member.id = :memberId
              and t.id in :termsIds
              and not exists (
                  select 1
                  from MemberTerms newer
                  where newer.member.id = mt.member.id
                    and newer.terms.id = mt.terms.id
                    and (
                        newer.createdAt > mt.createdAt
                        or (newer.createdAt = mt.createdAt and newer.id > mt.id)
                    )
              )
            """)
    List<MemberTerms> findLatestByMemberIdAndTermsIdIn(
            @Param("memberId") Long memberId,
            @Param("termsIds") List<Long> termsIds
    );

    long countByMember_IdAndTerms_Id(Long memberId, Long termsId);
}
