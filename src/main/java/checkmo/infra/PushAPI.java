package checkmo.infra;

import java.util.List;
import java.util.Map;

public interface PushAPI {

    List<PushSendResult> sendBatch(List<PushSendRequest> requests);

    Map<String, PushReceiptResult> getReceipts(List<String> ticketIds);
}
