package com.example.quanlychitieu;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

/**
 * Các hàm định dạng tiền và ngày tháng.
 * Trong cơ sở dữ liệu ngày lưu dạng yyyy-MM-dd (để ORDER BY và so sánh đúng),
 * còn ngoài giao diện hiển thị dạng dd/MM/yyyy.
 */
public final class FormatUtils {

    private FormatUtils() {
    }

    private static DecimalFormatSymbols vietnameseSymbols() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return symbols;
    }

    /** 1500000 -> "1.500.000 đ" */
    public static String formatMoney(long amount) {
        return new DecimalFormat("#,##0", vietnameseSymbols()).format(amount) + " đ";
    }

    /** 37.5 -> "37,5%" */
    public static String formatPercent(double percent) {
        return new DecimalFormat("0.#", vietnameseSymbols()).format(percent) + "%";
    }

    /** "2026-10-13" -> "13/10/2026" */
    public static String toDisplayDate(String isoDate) {
        if (isoDate == null) {
            return "";
        }
        String[] parts = isoDate.split("-");
        if (parts.length != 3) {
            return isoDate;
        }
        return parts[2] + "/" + parts[1] + "/" + parts[0];
    }

    /** (2026, 10, 13) -> "2026-10-13". Tháng tính từ 1 đến 12. */
    public static String toIsoDate(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, day);
    }

    /** Ngày hôm nay dạng yyyy-MM-dd. */
    public static String today() {
        Calendar c = Calendar.getInstance();
        return toIsoDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }
}
