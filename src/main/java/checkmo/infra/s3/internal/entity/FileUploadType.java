package checkmo.infra.s3.internal.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileUploadType {
    PROFILE("profiles"),
    CLUB("clubs"),
    NOTICE("notices");

    private final String path;
}
