package checkmo.domain.notification.service.command;

import checkmo.domain.member.entity.Member;
import checkmo.domain.member.facade.MemberQueryFacade;
import checkmo.domain.notification.converter.NotificationConverter;
import checkmo.domain.notification.entity.Notification;
import checkmo.domain.notification.repository.NotificationRepository;
import checkmo.event.FollowEvent;
import checkmo.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final MemberQueryFacade memberQueryFacade;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void createNotification(LikeEvent event) {
        // 책이야기에 좋아요를 누른 사람과 좋아요를 받은 사람의 정보를 가져옴 (프록시로)
        Member proxySender = memberQueryFacade.findMemberReferenceById(event.getSenderId());
        Member proxyReceiver = memberQueryFacade.findMemberReferenceById(event.getReceiverId());

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(Notification.NotificationType.LIKE, event.getBookStoryId());

        // Notification 객체를 생성하고 저장
        Notification notification = NotificationConverter.fromEvent(Notification.NotificationType.LIKE, redirectPath, proxySender, proxyReceiver);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void createNotification(FollowEvent event) {
        // 팔로우 이벤트에서 팔로우 누른 사람과 팔로우 당하는 사람의 정보를 가져옴 (프록시로)
        Member proxyFollower = memberQueryFacade.findMemberReferenceById(event.getFollowerId()); // 팔로우 누른 사람
        Member proxyFollowed = memberQueryFacade.findMemberReferenceById(event.getFollowedId()); // 팔로우 당하는 사람

        // 팔로우 누른 사람의 닉네임을 가져옴
        String FollowerNickname = memberQueryFacade.getMemberNicknameById(event.getFollowerId());

        // 리다이렉트 경로를 생성
        String redirectPath = NotificationConverter.getRedirectPath(Notification.NotificationType.FOLLOW, FollowerNickname);

        // Notification 객체를 생성하고 저장
        Notification notification = NotificationConverter.fromEvent(Notification.NotificationType.FOLLOW, redirectPath, proxyFollower, proxyFollowed);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(Long notificationId, String memberId) {
        throw new UnsupportedOperationException("아직 구현 X");
    }
}
