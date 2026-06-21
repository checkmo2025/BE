package checkmo;

import checkmo.book.internal.scheduler.BookRecommendationScheduler;
import checkmo.bookStory.internal.scheduler.BookStoryViewScheduler;
import checkmo.member.internal.scheduler.MemberCleanupScheduler;
import checkmo.support.SpringTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringTest
class CheckmoApplicationTests {

    @MockitoBean
    BookRecommendationScheduler bookRecommendationScheduler;

    @MockitoBean
    BookStoryViewScheduler bookStoryViewScheduler;

    @MockitoBean
    MemberCleanupScheduler memberCleanupScheduler;

    @DisplayName("각 모듈이 논리적으로 분리가 완료되었는지 Spring Modulith를 통해 확인합니다.")
    @Test
    void Spring_Modulith_Test() {
        ApplicationModules.of(CheckmoApplication.class).verify();
    }

    @Test
    void writeDocumentationSnippets() {
        ApplicationModules modules = ApplicationModules.of(CheckmoApplication.class);
        Documenter documenter = new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
        documenter.writeModuleCanvases();
    }
}
