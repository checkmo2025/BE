package checkmo.authentication.internal.security.auth;

import checkmo.authentication.internal.entity.AuthUser;
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

    private final AuthUser user;
    private final Map<String, Object> attributes;
    private final boolean newSocialSignUp;

    // 이건 이메일 로그인 시 사용하는 생성자
    public PrincipalDetails(AuthUser user) {
        this.user = user;
        this.attributes = null;
        this.newSocialSignUp = false;
    }

    // 이건 OAuth2 로그인 시 사용하는 생성자
    public PrincipalDetails(AuthUser user, Map<String, Object> attributes, boolean newSocialSignUp) {
        this.user = user;
        this.attributes = attributes;
        this.newSocialSignUp = newSocialSignUp;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getId();
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
        return true;
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
