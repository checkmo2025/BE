package checkmo.member.internal.entity;

import java.util.Comparator;

public enum TermsType {
    SERVICE_TERMS(1),
    PRIVACY_COLLECTION(2),
    THIRD_PARTY_PROVISION(3),
    MARKETING(4);

    public static final Comparator<TermsType> DISPLAY_ORDER =
            Comparator.comparingInt(TermsType::getDisplayOrder);

    private final int displayOrder;

    TermsType(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
