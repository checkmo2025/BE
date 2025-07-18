package checkmo.domain.bookStory.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Tag(name = "책 이야기", description = "책 이야기 업로드, 조회, 좋아요, 수정, 삭제 관련 API")
public class BookStoryController {
}
