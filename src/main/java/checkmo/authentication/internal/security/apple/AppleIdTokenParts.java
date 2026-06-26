package checkmo.authentication.internal.security.apple;

record AppleIdTokenParts(String header, String payload, String signature) {

    String signingInput() {
        return header + "." + payload;
    }
}
