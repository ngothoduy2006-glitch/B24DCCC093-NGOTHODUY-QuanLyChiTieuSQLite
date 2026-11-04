package com.example.quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Màn hình 2: Danh sách chi tiêu, có tìm kiếm theo tên và lọc theo danh mục.
 */
public class MainActivity extends AppCompatActivity {

    private EditText edtSearch;
    private Spinner spnCategory;
    private TextView tvTotal;
    private TextView tvEmpty;
    private RecyclerView rvExpenses;

    private ExpenseAdapter adapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        edtSearch = findViewById(R.id.edtSearch);
        spnCategory = findViewById(R.id.spnCategory);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvExpenses = findViewById(R.id.rvExpenses);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnStatistics = findViewById(R.id.btnStatistics);

        // Spinner lọc: "Tất cả danh mục" + 5 danh mục
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, Categories.withAll());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(spinnerAdapter);

        // RecyclerView: nhấn vào dòng -> mở chi tiết, chỉ truyền id
        adapter = new ExpenseAdapter(expense -> {
            Intent intent = new Intent(MainActivity.this, ExpenseDetailActivity.class);
            intent.putExtra(Expense.EXTRA_ID, expense.getId());
            startActivity(intent);
        });
        rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvExpenses.setAdapter(adapter);

        // Gõ đến đâu danh sách và tổng tiền cập nhật đến đó
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loadExpenses();
            }
        });

        // Đổi danh mục cũng tải lại (kết hợp được với ô tìm kiếm)
        spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadExpenses();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditExpenseActivity.class)));

        btnStatistics.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StatisticsActivity.class)));
    }

    /** Quay lại màn hình này (sau khi thêm, sửa, xóa) thì tải lại dữ liệu từ cơ sở dữ liệu. */
    @Override
    protected void onResume() {
        super.onResume();
        loadExpenses();
    }

    /** Đọc lại danh sách + tổng tiền theo bộ lọc hiện tại. */
    private void loadExpenses() {
        String keyword = edtSearch.getText().toString().trim();
        String category = (String) spnCategory.getSelectedItem();

        List<Expense> expenses = db.getExpenses(keyword, category);
        long total = db.getTotal(keyword, category);

        adapter.setItems(expenses);
        tvTotal.setText(getString(R.string.total_summary,
                FormatUtils.formatMoney(total), expenses.size()));

        boolean isEmpty = expenses.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvExpenses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
