package com.example.quanlychitieu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình 1: Đăng nhập. Tài khoản được kiểm tra bằng truy vấn bảng users.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private TextView tvLoginError;
    private Button btnLogin;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> attemptLogin());

        // Nhấn "Xong" trên bàn phím cũng đăng nhập
        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void attemptLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString();

        // Không cho đăng nhập khi để trống
        if (username.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.error_login_empty));
            return;
        }

        if (db.checkLogin(username, password)) {
            tvLoginError.setVisibility(View.GONE);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            showError(getString(R.string.error_login_wrong));
        }
    }

    private void showError(String message) {
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
