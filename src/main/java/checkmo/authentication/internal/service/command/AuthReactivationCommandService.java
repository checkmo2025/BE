package checkmo.authentication.internal.service.command;

import checkmo.authentication.AuthenticationEvent;
import checkmo.authentication.internal.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthReactivationCommandService {

    private final AuthRepository authRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void reactivateIfDeactivated(String memberId) {
        authRepository.findByIdAndDeactivatedAtIsNotNull(memberId)
                .ifPresent(authUser -> {
                    authUser.reactivate();
                    eventPublisher.publishEvent(
                            AuthenticationEvent.ReactivateMember.builder()
                                    .id(memberId)
                                    .build()
                    );
                });
    }
}
