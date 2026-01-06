package br.com.fivecom.litoralfm.ui.removal;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.ActivityRemovalBinding;
import br.com.fivecom.litoralfm.ui.views.alerts.DefaultDialog;
import br.com.fivecom.litoralfm.utils.constants.Constants;

public class RemovalActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Components();
    }

    private void Components() {
        ActivityRemovalBinding binding = ActivityRemovalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                || newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            Components();
    }

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return super.getOnBackInvokedDispatcher();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_removal) {
            new DefaultDialog(this).type(DefaultDialog.REDIRECT_URL).setName(R.string.string_removal3)
                    .setDesc(R.string.alert_download)
                    .url(Constants.data.redirect)
                    .start(android.R.string.ok, android.R.string.cancel);
        }
    }
}