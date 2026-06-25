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
            order by mt.createdAt desc, mt.id desc
            """)
    List<MemberTerms> findLatestCandidates(
            @Param("memberId") String memberId,
            @Param("termsIds") List<Long> termsIds
    );

    @Query("""
            select mt
            from MemberTerms mt
            join fetch mt.terms t
            where mt.member.id = :memberId
              and t.id = :termsId
            order by mt.createdAt desc, mt.id desc
            """)
    List<MemberTerms> findLatestCandidates(
            @Param("memberId") String memberId,
            @Param("termsId") Long termsId
    );

    @Query("""
            select count(mt)
            from MemberTerms mt
            where mt.member.id = :memberId
              and mt.terms.id = :termsId
            """)
    long countByMemberIdAndTermsId(
            @Param("memberId") String memberId,
            @Param("termsId") Long termsId
    );
}
