package com.example.quanlychitieu;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

/**
 * Màn hình 5: Thống kê tổng chi tiêu theo từng danh mục (truy vấn GROUP BY).
 */
public class StatisticsActivity extends AppCompatActivity {

    private TextView tvTotalAll;
    private LinearLayout layoutCategories;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = new DatabaseHelper(this);

        tvTotalAll = findViewById(R.id.tvTotalAll);
        layoutCategories = findViewById(R.id.layoutCategories);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }

    private void loadStatistics() {
        Map<String, Long> totals = db.getTotalByCategory();

        // Tổng tất cả danh mục
        long sum = 0;
        for (long value : totals.values()) {
            sum += value;
        }
        tvTotalAll.setText(FormatUtils.formatMoney(sum));

        // Mỗi danh mục một dòng: tên, tổng tiền, phần trăm, thanh ProgressBar
        layoutCategories.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Map.Entry<String, Long> entry : totals.entrySet()) {
            String category = entry.getKey();
            long amount = entry.getValue();
            double percent = sum > 0 ? amount * 100.0 / sum : 0;

            View row = inflater.inflate(R.layout.item_statistic, layoutCategories, false);
            TextView tvName = row.findViewById(R.id.tvStatName);
            TextView tvAmount = row.findViewById(R.id.tvStatAmount);
            TextView tvPercent = row.findViewById(R.id.tvStatPercent);
            ProgressBar progress = row.findViewById(R.id.progressStat);

            tvName.setText(category);
            tvAmount.setText(FormatUtils.formatMoney(amount));
            tvPercent.setText(FormatUtils.formatPercent(percent));

            progress.setMax(100);
            progress.setProgress((int) Math.round(percent));
            progress.setProgressTintList(ColorStateList.valueOf(Categories.colorOf(category)));
            progress.setProgressBackgroundTintList(ColorStateList.valueOf(0xFFE0E0E0));

            layoutCategories.addView(row);
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
