package br.com.fivecom.litoralfm.ui.about;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        Components();
    }

    private void Components() {
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                || newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            Components();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.bt_back)
            getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
