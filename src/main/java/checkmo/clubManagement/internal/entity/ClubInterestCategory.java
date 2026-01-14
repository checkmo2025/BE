package checkmo.clubManagement.internal.entity;

public enum ClubInterestCategory {
    FICTION_POETRY_DRAMA("소설/시/희곡"),
    ESSAY("에세이"),
    HUMANITIES("인문학"),
    SOCIAL_SCIENCE("사회 과학"),
    POLITICS_DIPLOMACY_DEFENSE("정치/외교"),
    ECONOMY_MANAGEMENT("경제/경영"),
    SELF_DEVELOPMENT("자기 계발"),
    HISTORY_CULTURE("역사/문화"),
    SCIENCE("과학"),
    COMPUTER_IT("컴퓨터/IT"),
    ART_POP_CULTURE("예술/대중 문화"),
    TRAVEL("여행"),
    FOREIGN_LANGUAGE("외국어"),
    CHILDREN_BOOKS("어린이 도서"),
    RELIGION_PHILOSOPHY("종교/철학");

    private final String description;

    ClubInterestCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
