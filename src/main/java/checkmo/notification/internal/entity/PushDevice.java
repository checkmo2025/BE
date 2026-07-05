package checkmo.notification.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_push_device_installation_id", columnNames = {"installation_id"}),
        @UniqueConstraint(name = "uk_push_device_expo_push_token", columnNames = {"expo_push_token"})
})
public class PushDevice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "installation_id", nullable = false, length = 36)
    private String installationId;

    @Column(name = "expo_push_token", nullable = false, length = 255)
    private String expoPushToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PushPlatform platform;

    @Column(name = "app_version", nullable = false, length = 32)
    private String appVersion;

    @Column(name = "build_number", nullable = false, length = 32)
    private String buildNumber;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "last_registered_at")
    private LocalDateTime lastRegisteredAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    public void update(String expoPushToken, String memberId, PushPlatform platform, String appVersion, String buildNumber) {
        this.expoPushToken = expoPushToken;
        this.memberId = memberId;
        this.platform = platform;
        this.appVersion = appVersion;
        this.buildNumber = buildNumber;
        this.active = true;
        this.lastRegisteredAt = LocalDateTime.now();
        this.deactivatedAt = null;
    }

    public void deactivate() {
        this.active = false;
        this.deactivatedAt = LocalDateTime.now();
    }

    public enum PushPlatform {
        IOS, ANDROID
    }
}
