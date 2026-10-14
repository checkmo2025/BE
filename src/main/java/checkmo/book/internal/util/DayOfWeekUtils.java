package checkmo.book.internal.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

public class DayOfWeekUtils {

    private DayOfWeekUtils() {}

    private static final int BOOKS_PER_DAY = 4;

    /**
     * 월요일: 0, 화요일: 4, 수요일: 8, 목요일: 12, 금요일: 16, 토요일: 20, 일요일: 24
     */
    public static int calculateStartIndexByDayOfWeek() {
        return calculateStartIndexByDayOfWeek(LocalDate.now(ZoneId.of("Asia/Seoul")));
    }

    public static int calculateStartIndexByDayOfWeek(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return (dayOfWeek.getValue() - 1) * BOOKS_PER_DAY;
    }
}
