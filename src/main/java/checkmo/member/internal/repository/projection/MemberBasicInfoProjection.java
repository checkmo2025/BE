package checkmo.member.internal.repository.projection;

/**
 * 회원 ID, 닉네임, 프로필 이미지
 */
public interface MemberBasicInfoProjection {
    String getId();
    String getNickName();
    String getImgUrl();
}