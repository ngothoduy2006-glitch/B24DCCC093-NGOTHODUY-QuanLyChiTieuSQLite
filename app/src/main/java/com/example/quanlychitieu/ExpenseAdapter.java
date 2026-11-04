package com.example.quanlychitieu;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách khoản chi trong RecyclerView.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    private final List<Expense> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public ExpenseAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Thay toàn bộ dữ liệu bằng danh sách mới đọc từ cơ sở dữ liệu. */
    public void setItems(List<Expense> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = items.get(position);

        holder.tvName.setText(expense.getName());
        holder.tvAmount.setText(FormatUtils.formatMoney(expense.getAmount()));
        holder.tvCategoryDate.setText(
                expense.getCategory() + "  •  " + FormatUtils.toDisplayDate(expense.getDate()));

        // Chữ cái đầu của danh mục trong vòng tròn màu
        String category = expense.getCategory();
        holder.tvAvatar.setText(category == null || category.isEmpty() ? "?" : category.substring(0, 1));
        holder.tvAvatar.setBackgroundTintList(ColorStateList.valueOf(Categories.colorOf(category)));

        holder.itemView.setOnClickListener(v -> listener.onItemClick(expense));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvName;
        final TextView tvCategoryDate;
        final TextView tvAmount;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategoryDate = itemView.findViewById(R.id.tvCategoryDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
