package checkmo.clubMeeting.internal.converter;

import static org.assertj.core.api.Assertions.assertThat;

import checkmo.clubMeeting.internal.entity.Topic;
import checkmo.clubMeeting.web.dto.bookshelf.BookShelfResponseDTO.TopicDetail;
import org.junit.jupiter.api.Test;

class ClubMeetingConverterTest {

    @Test
    void 발제_작성자_여부는_클럽_회원_ID가_아닌_회원_ID로_판별한다() {
        Long memberId = 100L;
        Topic topic = Topic.builder()
                .id(1L)
                .description("발제")
                .memberId(memberId)
                .clubMemberId(7L)
                .build();

        TopicDetail result = ClubMeetingConverter.toTopicDetailDTO(topic, null, memberId);

        assertThat(result.isAuthor()).isTrue();
    }
}
