package checkmo.infra.push;

import checkmo.infra.PushAPI;
import checkmo.infra.PushReceiptResult;
import checkmo.infra.PushSendRequest;
import checkmo.infra.PushSendResult;
import checkmo.infra.push.internal.dto.ExpoMessage;
import checkmo.infra.push.internal.dto.ExpoReceipt;
import checkmo.infra.push.internal.dto.ExpoTicket;
import checkmo.infra.push.internal.service.ExpoPushClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExpoPushApiImpl implements PushAPI {

    private final ExpoPushClient expoPushClient;

    @Override
    public List<PushSendResult> sendBatch(List<PushSendRequest> requests) {
        List<ExpoMessage> messages = requests.stream()
                .map(r -> r == null ? null
                        : new ExpoMessage(r.token(), r.title(), r.body(), r.data(), r.sound(), r.priority(), r.channelId()))
                .toList();

        List<ExpoTicket> tickets = expoPushClient.sendBatch(messages);

        return IntStream.range(0, requests.size())
                .mapToObj(i -> toSendResult(i < tickets.size() ? tickets.get(i) : null))
                .toList();
    }

    @Override
    public Map<String, PushReceiptResult> getReceipts(List<String> ticketIds) {
        return expoPushClient.getReceipts(ticketIds).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toReceiptResult(e.getValue())
                ));
    }

    private PushSendResult toSendResult(ExpoTicket ticket) {
        if (ticket == null) {
            return PushSendResult.failed("SEND_FAILED", "No ticket returned from Expo");
        }
        if ("ok".equals(ticket.status())) {
            return PushSendResult.ok(ticket.id());
        }
        String errorCode = ticket.details() != null ? ticket.details().error() : "UNKNOWN";
        return PushSendResult.failed(errorCode, ticket.message());
    }

    private PushReceiptResult toReceiptResult(ExpoReceipt receipt) {
        return new PushReceiptResult(receipt.status(), receipt.errorCode(), receipt.message());
    }
}
