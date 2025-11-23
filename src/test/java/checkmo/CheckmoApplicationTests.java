package checkmo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

@SpringBootTest
class CheckmoApplicationTests {

    @DisplayName("각 모듈이 논리적으로 분리가 완료되었는지 Spring Modulith를 통해 확인합니다.")
    @Test
    void Spring_Modulith_Test() {
        ApplicationModules modules = ApplicationModules.of(CheckmoApplication.class).verify();
        System.out.println(modules);
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
