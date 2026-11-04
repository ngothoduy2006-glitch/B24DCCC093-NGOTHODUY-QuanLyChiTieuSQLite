package com.example.quanlychitieu;


public class Expense {

    /** Khóa Intent dùng để truyền id khoản chi giữa các Activity. */
    public static final String EXTRA_ID = "expense_id";

    private int id;
    private String name;
    private long amount;
    private String category;
    private String date;   // lưu dạng yyyy-MM-dd
    private String note;

    public Expense() {
    }

    public Expense(String name, long amount, String category, String date, String note) {
        this(0, name, amount, category, date, note);
    }

    public Expense(int id, String name, long amount, String category, String date, String note) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
