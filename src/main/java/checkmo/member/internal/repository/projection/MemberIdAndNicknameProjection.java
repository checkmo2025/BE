package checkmo.member.internal.repository.projection;

/**
 * 회원 ID와 닉네임
 */
public interface MemberIdAndNicknameProjection {
    Long getId();
    String getNickName();
}
