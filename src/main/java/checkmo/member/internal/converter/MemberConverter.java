package checkmo.member.internal.converter;

import checkmo.member.MemberExternalDTO;
import checkmo.member.internal.entity.Member;
import checkmo.member.internal.entity.MemberReport;
import checkmo.member.web.dto.MemberResponseDTO.BasicInfoWithDescription;
import checkmo.member.web.dto.MemberResponseDTO.BasicInfoWithFollow;
import checkmo.member.web.dto.MemberResponseDTO.DetailInfo;
import checkmo.member.web.dto.MemberResponseDTO.MyReportInfo;
import checkmo.member.web.dto.MemberResponseDTO.RecommendedMember;
import checkmo.member.web.dto.MemberResponseDTO.ReportInfo;
import checkmo.member.web.dto.MemberResponseDTO.othersDetailInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberConverter {

    public static BasicInfoWithDescription toMemberProfileWithProfileImage(Member member) {
        return BasicInfoWithDescription.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    public static DetailInfo toMemberProfileWithCategory(Member member) {
        return DetailInfo.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .build();
    }

    public static othersDetailInfo toOtherProfile(
            Member member,
            boolean isFollowing,
            long followerCount,
            long followingCount
    ) {
        return othersDetailInfo.builder()
                .nickname(member.getNickName())
                .description(member.getDescription())
                .profileImageUrl(member.getImgUrl())
                .categories(member.getInterestCategories())
                .following(isFollowing)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }

    public static MemberExternalDTO.BasicInfoWithFollow toMemberProfileWithFollowStatus(
            BasicInfoWithFollow profile
    ) {
        return MemberExternalDTO.BasicInfoWithFollow.builder()
                .nickname(profile.getNickname())
                .profileImageUrl(profile.getProfileImageUrl())
                .following(profile.isFollowing())
                .build();
    }

    public static RecommendedMember toRecommendedMember(Member member) {
        return RecommendedMember.builder()
                .nickname(member.getNickName())
                .profileImageUrl(member.getImgUrl())
                .build();
    }

    public static ReportInfo toReportInfo(MemberReport report) {
        return ReportInfo.builder()
                .reportId(report.getId())
                .reportedMemberNickname(report.getReportedMember().getNickName())
                .reportedMemberProfileImageUrl(report.getReportedMember().getImgUrl())
                .reportType(report.getReportType().getDescription())
                .content(report.getContent())
                .createdAt(report.getCreatedAt())
                .build();
    }

    public static MyReportInfo toMyReportInfo(MemberReport report) {
        return MyReportInfo.builder()
                .reportedMemberNickname(report.getReportedMember().getNickName())
                .reportedMemberProfileImageUrl(report.getReportedMember().getImgUrl())
                .reportType(report.getReportType().name())
                .content(report.getContent())
                .reportDate(report.getCreatedAt())
                .build();
    }
}
