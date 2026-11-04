package com.example.quanlychitieu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class AddEditExpenseActivity extends AppCompatActivity {

    private static final int NO_ID = -1;

    private TextView tvScreenTitle;
    private TextInputLayout tilName;
    private TextInputLayout tilAmount;
    private TextInputEditText edtName;
    private TextInputEditText edtAmount;
    private TextInputEditText edtNote;
    private Spinner spnCategory;
    private DatePicker datePicker;
    private TextView tvDateError;
    private Button btnSave;
    private Button btnCancel;

    private DatabaseHelper db;
    private int expenseId = NO_ID;   // NO_ID = chế độ thêm mới

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        db = new DatabaseHelper(this);

        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tilName = findViewById(R.id.tilName);
        tilAmount = findViewById(R.id.tilAmount);
        edtName = findViewById(R.id.edtName);
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        spnCategory = findViewById(R.id.spnCategory);
        datePicker = findViewById(R.id.datePicker);
        tvDateError = findViewById(R.id.tvDateError);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        // Spinner danh mục (5 danh mục, không có "Tất cả")
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Categories.LIST);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(spinnerAdapter);

        // Không cho chọn ngày sau ngày hiện tại
        datePicker.setMaxDate(System.currentTimeMillis());

        // Xóa thông báo lỗi khi người dùng bắt đầu sửa lại
        edtName.addTextChangedListener(new ClearErrorWatcher(tilName));
        edtAmount.addTextChangedListener(new ClearErrorWatcher(tilAmount));

        // Phân biệt chế độ thêm / sửa bằng Intent
        expenseId = getIntent().getIntExtra(Expense.EXTRA_ID, NO_ID);
        if (expenseId == NO_ID) {
            tvScreenTitle.setText(R.string.add_title);
            btnSave.setText(R.string.btn_save_add);
        } else {
            tvScreenTitle.setText(R.string.edit_title);
            btnSave.setText(R.string.btn_save_edit);
            loadExpenseIntoForm();
        }

        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> finish());
    }

    /** Chế độ sửa: đọc dữ liệu cũ từ cơ sở dữ liệu theo id và hiển thị lên form. */
    private void loadExpenseIntoForm() {
        Expense expense = db.getExpenseById(expenseId);
        if (expense == null) {
            Toast.makeText(this, R.string.toast_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        edtName.setText(expense.getName());
        edtAmount.setText(String.valueOf(expense.getAmount()));
        edtNote.setText(expense.getNote());
        spnCategory.setSelection(Categories.indexOf(expense.getCategory()));

        // Ngày lưu dạng yyyy-MM-dd -> đặt lại cho DatePicker (tháng của DatePicker tính từ 0)
        try {
            String[] parts = expense.getDate().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            datePicker.updateDate(year, month - 1, day);
        } catch (RuntimeException ex) {
            // Dữ liệu ngày không đúng định dạng: giữ nguyên ngày hôm nay
        }
    }

    private void saveExpense() {
        String name = getText(edtName);
        String amountText = getText(edtAmount);
        String note = getText(edtNote);
        String category = (String) spnCategory.getSelectedItem();
        String date = FormatUtils.toIsoDate(
                datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());

        boolean valid = true;

        // Tên không được để trống
        if (name.isEmpty()) {
            tilName.setError(getString(R.string.error_name_empty));
            valid = false;
        }

        // Số tiền: bắt buộc, là số nguyên lớn hơn 0
        long amount = 0;
        if (amountText.isEmpty()) {
            tilAmount.setError(getString(R.string.error_amount_empty));
            valid = false;
        } else {
            try {
                amount = Long.parseLong(amountText);
                if (amount <= 0) {
                    tilAmount.setError(getString(R.string.error_amount_invalid));
                    valid = false;
                }
            } catch (NumberFormatException ex) {
                tilAmount.setError(getString(R.string.error_amount_invalid));
                valid = false;
            }
        }

        // Ngày không được sau ngày hiện tại (yyyy-MM-dd so sánh chuỗi được)
        if (date.compareTo(FormatUtils.today()) > 0) {
            tvDateError.setText(R.string.error_date_future);
            tvDateError.setVisibility(View.VISIBLE);
            valid = false;
        } else {
            tvDateError.setVisibility(View.GONE);
        }

        if (!valid) {
            return;
        }

        if (expenseId == NO_ID) {
            // Chế độ thêm: INSERT
            long newId = db.insertExpense(new Expense(name, amount, category, date, note));
            if (newId == -1) {
                Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show();
        } else {
            // Chế độ sửa: UPDATE theo id
            int rows = db.updateExpense(new Expense(expenseId, name, amount, category, date, note));
            if (rows <= 0) {
                Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show();
        }

        // Quay về màn hình trước, màn hình đó sẽ tải lại dữ liệu trong onResume()
        finish();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    /** Tự xóa lỗi của TextInputLayout khi nội dung ô nhập thay đổi. */
    private static class ClearErrorWatcher implements TextWatcher {
        private final TextInputLayout layout;

        ClearErrorWatcher(TextInputLayout layout) {
            this.layout = layout;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            layout.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
