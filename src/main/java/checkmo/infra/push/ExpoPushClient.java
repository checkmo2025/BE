package checkmo.infra.push;

import checkmo.infra.push.internal.config.properties.ExpoPushProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpoPushClient {

    private static final int MAX_BATCH_SIZE = 100;

    private final RestClient expoPushRestClient;
    private final ExpoPushProperties properties;

    /**
     * 메시지 목록을 Expo Push Service에 발송한다.
     * 반환 리스트는 입력 messages와 동일한 순서·길이를 보장한다.
     * HTTP 오류로 청크 전체가 실패한 경우 해당 인덱스는 null로 채워진다.
     */
    public List<ExpoTicket> sendBatch(List<ExpoMessage> messages) {
        if (!properties.isEnabled() || messages.isEmpty()) {
            return Collections.nCopies(messages.size(), null);
        }

        List<ExpoTicket> result = new ArrayList<>(messages.size());

        for (List<ExpoMessage> chunk : partition(messages, MAX_BATCH_SIZE)) {
            try {
                List<ExpoTicket> tickets = sendChunk(chunk);
                result.addAll(tickets);
            } catch (Exception e) {
                log.error("Expo 푸시 발송 실패: count={}, error={}", chunk.size(), e.getMessage());
                result.addAll(Collections.nCopies(chunk.size(), null));
            }
        }

        return result;
    }

    /**
     * ticket ID 목록으로 delivery receipt를 조회한다.
     * enabled=false인 경우 빈 맵을 반환한다 (로컬·테스트 환경 보호).
     * 반환 맵의 키는 ticketId이며, 응답에 없는 ticketId는 포함되지 않는다.
     */
    public Map<String, ExpoReceipt> getReceipts(List<String> ticketIds) {
        if (!properties.isEnabled() || ticketIds.isEmpty()) {
            return Map.of();
        }

        try {
            ReceiptResponse response = expoPushRestClient.post()
                    .uri("/push/getReceipts")
                    .body(new ReceiptRequest(ticketIds))
                    .retrieve()
                    .body(ReceiptResponse.class);

            return (response != null && response.data() != null) ? response.data() : Map.of();
        } catch (Exception e) {
            log.error("Expo receipt 조회 실패: count={}, error={}", ticketIds.size(), e.getMessage());
            return Map.of();
        }
    }

    private List<ExpoTicket> sendChunk(List<ExpoMessage> chunk) {
        ExpoSendResponse response = expoPushRestClient.post()
                .uri("/push/send")
                .body(chunk)
                .retrieve()
                .body(ExpoSendResponse.class);

        if (response == null || response.data() == null) {
            return Collections.nCopies(chunk.size(), null);
        }
        return response.data();
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ExpoSendResponse(List<ExpoTicket> data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReceiptRequest(List<String> ids) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ReceiptResponse(Map<String, ExpoReceipt> data) {}
}
