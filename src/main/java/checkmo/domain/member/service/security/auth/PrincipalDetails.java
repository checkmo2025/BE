package checkmo.domain.member.service.security.auth;

import checkmo.domain.member.entity.Member;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final Member member;
    private final Map<String, Object> attributes;

    // 이건 이메일 로그인 시 사용하는 생성자
    public PrincipalDetails(Member member) {
        this.member = member;
        this.attributes = null;
    }

    // 이건 OAuth2 로그인 시 사용하는 생성자
    public PrincipalDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return member.getDeactivated() == null;
    }

    // OAuth2User methods
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
