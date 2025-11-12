package checkmo.member.internal.entity;

/**
 * 회원의 관심 도서 카테고리
 * Member 도메인 내부에서만 사용하며, Club과는 별도로 관리
 */
public enum MemberInterestCategory {

    NOVEL,
    ESSAY,
    POEM,
    SELF_DEVELOPMENT,
    HUMANITIES,
    HISTORY,
    SCIENCE,
    ECONOMICS,
    SOCIETY,
    ART
}