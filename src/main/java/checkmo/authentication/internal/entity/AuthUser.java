package checkmo.authentication.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class AuthUser extends BaseEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean profileCompleted;

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

    public void changePassword(String newPassword) {
        this.password = newPassword;
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
}
