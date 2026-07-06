package checkmo.report.internal.entity;

import checkmo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType reportTargetType;

    @Column(nullable = false)
    private String targetId; // 신고 대상의 ID가 Long일 경우 String으로 변환

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reportReason;

    @Column(length = 500)
    private String content;

    @Column(nullable = false, length = 500)
    private String redirectUrl;

}
