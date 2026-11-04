package com.example.quanlychitieu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản lý cơ sở dữ liệu SQLite expense_manager.db (phiên bản 1) gồm 2 bảng:
 * users (tài khoản đăng nhập) và expenses (khoản chi).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "expense_manager.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_EXPENSES = "expenses";

    // ====================================================================
    // Câu lệnh CREATE TABLE
    // ====================================================================
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL)";

    private static final String SQL_CREATE_EXPENSES =
            "CREATE TABLE expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "amount INTEGER NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "note TEXT)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ====================================================================
    // Tạo bảng + chèn dữ liệu khởi tạo (chỉ chạy một lần khi file .db chưa tồn tại)
    // ====================================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_EXPENSES);

        // INSERT tài khoản mẫu
        db.execSQL("INSERT INTO users (username, password) VALUES ('admin', '123456')");

        // INSERT 3 khoản chi mẫu (cột note để trống = NULL)
        db.execSQL("INSERT INTO expenses (name, amount, category, date) " +
                "VALUES ('Ăn sáng', 30000, 'Ăn uống', '2026-10-13')");
        db.execSQL("INSERT INTO expenses (name, amount, category, date) " +
                "VALUES ('Đổ xăng', 50000, 'Đi lại', '2026-10-12')");
        db.execSQL("INSERT INTO expenses (name, amount, category, date) " +
                "VALUES ('Mua sách', 120000, 'Học tập', '2026-10-11')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Bài thực hành: khi đổi phiên bản thì xóa bảng cũ và tạo lại
        db.execSQL("DROP TABLE IF EXISTS expenses");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ====================================================================
    // Đăng nhập
    // ====================================================================

    /** Trả về true nếu có dòng khớp username + password trong bảng users. */
    public boolean checkLogin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[]{username, password})) {
            return cursor.moveToFirst();
        }
    }

    // ====================================================================
    // Thêm / Sửa / Xóa
    // ====================================================================

    /**
     * Thêm khoản chi. Tương đương:
     * INSERT INTO expenses (name, amount, category, date, note) VALUES (?, ?, ?, ?, ?)
     *
     * @return id vừa thêm, hoặc -1 nếu lỗi
     */
    public long insertExpense(Expense e) {
        return getWritableDatabase().insert(TABLE_EXPENSES, null, toContentValues(e));
    }

    /**
     * Cập nhật khoản chi theo id. Tương đương:
     * UPDATE expenses SET name = ?, amount = ?, category = ?, date = ?, note = ? WHERE id = ?
     *
     * @return số dòng được cập nhật (0 nếu không tìm thấy id)
     */
    public int updateExpense(Expense e) {
        return getWritableDatabase().update(TABLE_EXPENSES, toContentValues(e),
                "id = ?", new String[]{String.valueOf(e.getId())});
    }

    /**
     * Xóa khoản chi theo id. Tương đương: DELETE FROM expenses WHERE id = ?
     *
     * @return số dòng đã xóa
     */
    public int deleteExpense(int id) {
        return getWritableDatabase().delete(TABLE_EXPENSES,
                "id = ?", new String[]{String.valueOf(id)});
    }

    // ====================================================================
    // Truy vấn
    // ====================================================================

    /** SELECT * FROM expenses WHERE id = ?  (trả về null nếu không có). */
    public Expense getExpenseById(int id) {
        String sql = "SELECT id, name, amount, category, date, note FROM expenses WHERE id = ?";
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return cursorToExpense(cursor);
            }
            return null;
        }
    }

    /**
     * Danh sách khoản chi: tên chứa keyword (LIKE), lọc theo danh mục
     * (bỏ qua nếu là "Tất cả danh mục"), ngày mới nhất xếp trước. Ví dụ đầy đủ:
     * SELECT id, name, amount, category, date, note FROM expenses
     * WHERE name LIKE ? ESCAPE '\' AND category = ?
     * ORDER BY date DESC, id DESC
     */
    public List<Expense> getExpenses(String keyword, String category) {
        List<String> args = new ArrayList<>();
        String sql = "SELECT id, name, amount, category, date, note FROM expenses"
                + buildWhere(keyword, category, args)
                + " ORDER BY date DESC, id DESC";

        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]))) {
            while (cursor.moveToNext()) {
                list.add(cursorToExpense(cursor));
            }
        }
        return list;
    }

    /**
     * Tổng số tiền theo cùng điều kiện lọc với getExpenses():
     * SELECT SUM(amount) FROM expenses WHERE name LIKE ? ESCAPE '\' AND category = ?
     */
    public long getTotal(String keyword, String category) {
        List<String> args = new ArrayList<>();
        String sql = "SELECT SUM(amount) FROM expenses" + buildWhere(keyword, category, args);

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args.toArray(new String[0]))) {
            // SUM trả về NULL khi không có dòng nào; getLong() của NULL cho kết quả 0
            return cursor.moveToFirst() ? cursor.getLong(0) : 0L;
        }
    }

    /**
     * Tổng tiền theo từng danh mục:
     * SELECT category, SUM(amount) FROM expenses GROUP BY category
     * Kết quả luôn có đủ 5 danh mục, danh mục chưa có khoản chi sẽ là 0.
     */
    public Map<String, Long> getTotalByCategory() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (String category : Categories.LIST) {
            result.put(category, 0L);
        }

        String sql = "SELECT category, SUM(amount) FROM expenses GROUP BY category";
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, null)) {
            while (cursor.moveToNext()) {
                result.put(cursor.getString(0), cursor.getLong(1));
            }
        }
        return result;
    }

    // ====================================================================
    // Hàm hỗ trợ nội bộ
    // ====================================================================

    /**
     * Ghép mệnh đề WHERE từ điều kiện tìm kiếm và lọc danh mục.
     * Giá trị người dùng nhập luôn đi qua tham số "?" (args), không nối trực tiếp vào câu SQL.
     */
    private String buildWhere(String keyword, String category, List<String> args) {
        StringBuilder where = new StringBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" WHERE name LIKE ? ESCAPE '\\'");
            args.add("%" + escapeLike(keyword.trim()) + "%");
        }

        if (category != null && !category.isEmpty() && !category.equals(Categories.ALL)) {
            where.append(where.length() == 0 ? " WHERE" : " AND");
            where.append(" category = ?");
            args.add(category);
        }
        return where.toString();
    }

    /** Vô hiệu hóa các ký tự đặc biệt của LIKE (% và _) trong từ khóa người dùng gõ. */
    private String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private ContentValues toContentValues(Expense e) {
        ContentValues values = new ContentValues();
        values.put("name", e.getName());
        values.put("amount", e.getAmount());
        values.put("category", e.getCategory());
        values.put("date", e.getDate());
        values.put("note", e.getNote());
        return values;
    }

    private Expense cursorToExpense(Cursor c) {
        String note = c.getString(c.getColumnIndexOrThrow("note"));
        return new Expense(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getLong(c.getColumnIndexOrThrow("amount")),
                c.getString(c.getColumnIndexOrThrow("category")),
                c.getString(c.getColumnIndexOrThrow("date")),
                note == null ? "" : note
        );
    }
}
