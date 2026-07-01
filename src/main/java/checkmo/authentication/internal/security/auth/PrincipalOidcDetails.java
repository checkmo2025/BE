package checkmo.authentication.internal.security.auth;

import checkmo.authentication.internal.entity.AuthUser;
import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class PrincipalOidcDetails extends PrincipalDetails implements OidcUser {

    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public PrincipalOidcDetails(
            AuthUser user,
            Map<String, Object> attributes,
            boolean newSocialSignUp,
            OidcIdToken idToken,
            OidcUserInfo userInfo
    ) {
        super(user, attributes, newSocialSignUp);
        this.idToken = idToken;
        this.userInfo = userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return getAttributes();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}
