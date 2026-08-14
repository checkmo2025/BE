package checkmo.authentication.internal.entity;

import checkmo.common.BaseEntity;
import checkmo.common.nickname.NicknamePolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_auth_user_nickname_key", columnNames = "nick_name_key")
        }
)
public class AuthUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String legacyId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String providerUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean profileCompleted;

    @Column(length = 20)
    private String nickName;

    @Column(name = "nick_name_key", length = 20)
    private String nickNameKey;

    private LocalDateTime deactivatedAt;

    public void deactivate() {
        this.deactivatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.deactivatedAt = null;
    }

    public void completeProfile() {
        this.profileCompleted = true;
    }

    public boolean isActive() {
        return deactivatedAt == null;
    }

    public boolean isDeactivated() {
        return deactivatedAt != null;
    }

    public void updatePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateNickname(String nickName) {
        this.nickName = NicknamePolicy.normalizeForStorage(nickName);
        this.nickNameKey = NicknamePolicy.comparisonKey(this.nickName);
    }

    @PrePersist
    @PreUpdate
    private void synchronizeNicknameIdentity() {
        updateNickname(nickName);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
}
