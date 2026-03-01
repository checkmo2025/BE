package checkmo.authentication.internal.entity;

public abstract class Provider {

    public static final String GOOGLE = "google";
    public static final String KAKAO = "kakao";
    public static final String NAVER = "naver";

    public static abstract class Google {
        public static final String EMAIL = "email";
        public static final String PROVIDER_ID = "sub";
    }

    public static abstract class Kakao {
        public static final String ACCOUNT = "kakao_account";
        public static final String EMAIL = "email";
        public static final String PROVIDER_ID = "id";
    }

    public static abstract class Naver {
        public static final String RESPONSE = "response";
        public static final String EMAIL = "email";
        public static final String PROVIDER_ID = "id";
    }
}
