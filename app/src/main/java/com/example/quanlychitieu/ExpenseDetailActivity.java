package com.example.quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Màn hình 4: Chi tiết khoản chi. Nhận id qua Intent rồi tự truy vấn lại cơ sở dữ liệu.
 */
public class ExpenseDetailActivity extends AppCompatActivity {

    private TextView tvDetailName;
    private TextView tvDetailAmount;
    private TextView tvDetailCategory;
    private TextView tvDetailDate;
    private TextView tvDetailNote;

    private DatabaseHelper db;
    private int expenseId = -1;
    private String expenseName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_detail);

        db = new DatabaseHelper(this);
        expenseId = getIntent().getIntExtra(Expense.EXTRA_ID, -1);

        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailAmount = findViewById(R.id.tvDetailAmount);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailNote = findViewById(R.id.tvDetailNote);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnDelete = findViewById(R.id.btnDelete);

        // Sửa: truyền id sang AddEditExpenseActivity (chế độ sửa)
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ExpenseDetailActivity.this, AddEditExpenseActivity.class);
            intent.putExtra(Expense.EXTRA_ID, expenseId);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    /** Tải lại dữ liệu mỗi khi quay về màn hình (ví dụ sau khi sửa xong). */
    @Override
    protected void onResume() {
        super.onResume();
        loadExpense();
    }

    private void loadExpense() {
        Expense expense = db.getExpenseById(expenseId);
        if (expense == null) {
            Toast.makeText(this, R.string.toast_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        expenseName = expense.getName();
        tvDetailName.setText(expense.getName());
        tvDetailAmount.setText(FormatUtils.formatMoney(expense.getAmount()));
        tvDetailCategory.setText(expense.getCategory());
        tvDetailDate.setText(FormatUtils.toDisplayDate(expense.getDate()));

        String note = expense.getNote();
        tvDetailNote.setText(note == null || note.isEmpty() ? getString(R.string.no_note) : note);
    }

    /** Hộp thoại xác nhận xóa (Hủy / Xóa). */
    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, expenseName))
                .setNegativeButton(R.string.dialog_cancel, null)
                .setPositiveButton(R.string.dialog_delete, (dialog, which) -> {
                    db.deleteExpense(expenseId);
                    Toast.makeText(ExpenseDetailActivity.this,
                            R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                    finish();   // quay về danh sách, danh sách tự tải lại trong onResume()
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
