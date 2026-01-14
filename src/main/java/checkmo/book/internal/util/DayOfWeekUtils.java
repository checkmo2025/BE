package checkmo.book.internal.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DayOfWeekUtils {

    private DayOfWeekUtils() {}

    private static final int BOOKS_PER_DAY = 4;

    /**
     * 월요일: 0, 화요일: 4, 수요일: 8, 목요일: 12, 금요일: 16, 토요일: 20, 일요일: 24
     */
    public static int calculateStartIndexByDayOfWeek() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();
        return (dayOfWeek.getValue() - 1) * BOOKS_PER_DAY;
    }
}
