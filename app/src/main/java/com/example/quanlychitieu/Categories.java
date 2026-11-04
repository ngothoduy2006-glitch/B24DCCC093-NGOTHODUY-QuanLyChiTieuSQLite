package com.example.quanlychitieu;

/**
 * Danh sách danh mục chi tiêu và màu đại diện cho từng danh mục.
 */
public final class Categories {

    /** Mục đầu tiên của Spinner lọc: không lọc theo danh mục. */
    public static final String ALL = "Tất cả danh mục";

    /** 5 danh mục chi tiêu. */
    public static final String[] LIST = {"Ăn uống", "Đi lại", "Học tập", "Giải trí", "Khác"};

    private static final int[] COLORS = {
            0xFFFF7043,  // Ăn uống  - cam
            0xFF42A5F5,  // Đi lại   - xanh dương
            0xFF66BB6A,  // Học tập  - xanh lá
            0xFFAB47BC,  // Giải trí - tím
            0xFF78909C   // Khác     - xám xanh
    };

    private Categories() {
    }

    /** Danh sách dùng cho Spinner lọc ở màn hình danh sách: "Tất cả danh mục" + 5 danh mục. */
    public static String[] withAll() {
        String[] result = new String[LIST.length + 1];
        result[0] = ALL;
        System.arraycopy(LIST, 0, result, 1, LIST.length);
        return result;
    }

    /** Vị trí của danh mục trong LIST, không tìm thấy thì trả về 0. */
    public static int indexOf(String category) {
        for (int i = 0; i < LIST.length; i++) {
            if (LIST[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }

    /** Màu đại diện của danh mục. */
    public static int colorOf(String category) {
        return COLORS[indexOf(category)];
    }
}
