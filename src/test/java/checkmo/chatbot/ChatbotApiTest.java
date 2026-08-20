package checkmo.chatbot;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import checkmo.chatbot.internal.service.ChatOrchestrationService;
import checkmo.chatbot.internal.service.ChatReply;
import checkmo.support.ApiTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class ChatbotApiTest extends ApiTestSupport {

    @MockitoBean
    ChatOrchestrationService chatOrchestrationService;

    @Test
    void 비로그인_사용자도_챗봇_메시지를_보낼_수_있다() {
        when(chatOrchestrationService.respond(any(), eq(null), anyString())).thenReturn(sampleReply());

        given()
                .contentType("application/json")
                .body("{\"message\": \"책이야기 쓰려면?\"}")
                .when()
                .post("/api/v1/chatbot/messages")
                .then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("result.sessionToken", equalTo("session-abc"))
                .body("result.replyText", equalTo("책 이야기는 하단 탭에서 작성할 수 있어요."))
                .body("result.modelUsed", equalTo("gemini-3.1-flash-lite"))
                .body("result.handoffSuggested", equalTo(false));
    }

    @Test
    void 로그인_사용자의_요청은_memberId와_함께_전달된다() {
        TestUser user = createUser();
        when(chatOrchestrationService.respond(any(), any(), anyString())).thenReturn(sampleReply());

        given()
                .cookie(accessTokenCookie(user))
                .contentType("application/json")
                .body("{\"message\": \"모임 가입하려면?\"}")
                .when()
                .post("/api/v1/chatbot/messages")
                .then()
                .statusCode(200);

        verify(chatOrchestrationService).respond(eq(null), any(Long.class), eq("모임 가입하려면?"));
    }

    @Test
    void 이전_세션토큰을_실어_보내면_그대로_전달된다() {
        when(chatOrchestrationService.respond(eq("existing-token"), eq(null), anyString()))
                .thenReturn(sampleReply());

        given()
                .contentType("application/json")
                .body("{\"sessionToken\": \"existing-token\", \"message\": \"여전히 안 돼요\"}")
                .when()
                .post("/api/v1/chatbot/messages")
                .then()
                .statusCode(200);

        verify(chatOrchestrationService).respond(eq("existing-token"), eq(null), eq("여전히 안 돼요"));
    }

    @Test
    void 메시지가_비어있으면_400을_반환한다() {
        given()
                .contentType("application/json")
                .body("{\"message\": \"\"}")
                .when()
                .post("/api/v1/chatbot/messages")
                .then()
                .statusCode(400);
    }

    private ChatReply sampleReply() {
        return new ChatReply(
                "session-abc",
                "책 이야기는 하단 탭에서 작성할 수 있어요.",
                false,
                "gemini-3.1-flash-lite",
                false,
                "https://www.checkmo.co.kr/support",
                "https://docs.google.com/forms/d/e/1FAIpQLSfcY9nWElffO0gbjRlxFzEV4YKCOznMsv4PqfFO8MjgR1xaBg/viewform"
        );
    }
}
