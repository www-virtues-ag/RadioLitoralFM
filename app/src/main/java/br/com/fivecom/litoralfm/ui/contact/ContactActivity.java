package br.com.fivecom.litoralfm.ui.contact;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.ActivityContactBinding;
import br.com.fivecom.litoralfm.models.email.DDIInfo;
import br.com.fivecom.litoralfm.models.email.Email;
import br.com.fivecom.litoralfm.ui.views.alerts.DefaultDialog;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.constants.Preferences;
import br.com.fivecom.litoralfm.utils.core.Logger;
import br.com.fivecom.litoralfm.utils.core.Masks;
import br.com.fivecom.litoralfm.utils.requests.RequestListener;
import br.com.fivecom.litoralfm.utils.requests.RequestManager;

public class ContactActivity extends AppCompatActivity implements RequestListener<Email>, View.OnClickListener {

    private ActivityContactBinding binding;
    private RequestManager requestManager;
    private ProgressDialog progressDialog;
    private Preferences preferences;
    private Bundle savedState;

    // Relógio em tempo real
    private Handler clockHandler;
    private Runnable clockRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
        preferences = new Preferences(this);
        requestManager = new RequestManager();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.string_sending));
        progressDialog.setCancelable(false);
        savedState = savedInstanceState;
        Components();
    }

    private void Components() {
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.edtMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) binding.btSend.performClick();
            return false;
        });
        binding.edtPhone.addTextChangedListener(Masks.add(binding.edtPhone, "(##) #####-####", null));
        
        // Configurar listeners dos botões
        binding.btBack.setOnClickListener(this);
        binding.btSend.setOnClickListener(this);

        // Atualizar texto do TextView baseado no tipo de contato
        updateContactTypeText();

        restoreFields();
    }

    /**
     * Atualiza o texto do TextView txt_advertise baseado no tipo de contato recebido via Intent
     */
    private void updateContactTypeText() {
        String contactType = getIntent().getStringExtra("contact_type");
        if (contactType != null && !contactType.isEmpty() && binding.txtAdvertise != null) {
            binding.txtAdvertise.setText(contactType);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_back:
                finish(); // Volta para a tela anterior (fecha a activity)
                break;
            case R.id.bt_send:
                if (empty(binding.edtName) && email(binding.edtEmail)
                        && empty(binding.edtPhone, 4)
                        && empty(binding.edtMessage))
                    requestManager.fetchEmail(this, 0, getString(R.string.url_lab), binding.edtName.getText().toString(),
                            binding.edtEmail.getText().toString(), getString(R.string.nav_contact), binding.edtMessage.getText().toString(), "",
                            String.valueOf(Constants.data.id_app), String.valueOf(Constants.data.radios.get(Constants.ID).id),
                            "Android " + Build.VERSION.RELEASE, Build.MANUFACTURER + " - " + Build.MODEL);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void saveFields() {
        if (binding != null) {
            if (savedState == null) {
                savedState = new Bundle();
            }
            savedState.putString("edtName", binding.edtName.getText() != null ? binding.edtName.getText().toString() : "");
            savedState.putString("edtEmail", binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString() : "");
            savedState.putString("edtPhone", binding.edtPhone.getText() != null ? binding.edtPhone.getText().toString() : "");
            savedState.putString("edtMessage", binding.edtMessage.getText() != null ? binding.edtMessage.getText().toString() : "");
        }
    }

    private void restoreFields() {
        if (savedState != null && binding != null) {
            String name = savedState.getString("edtName", "");
            String email = savedState.getString("edtEmail", "");
            String phone = savedState.getString("edtPhone", "");
            String message = savedState.getString("edtMessage", "");

            if (!name.isEmpty()) binding.edtName.setText(name);
            if (!email.isEmpty()) binding.edtEmail.setText(email);
            if (!phone.isEmpty()) binding.edtPhone.setText(phone);
            if (!message.isEmpty()) binding.edtMessage.setText(message);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveFields();
        if (savedState != null) {
            outState.putAll(savedState);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                || newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            saveFields();
            Components();
            restoreFields();
        }
    }

    @NonNull
    @Override
    public OnBackInvokedDispatcher getOnBackInvokedDispatcher() {
        return super.getOnBackInvokedDispatcher();
    }

    @Override
    protected void onDestroy() {
        // Parar atualização do relógio
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
        requestManager.cancel();
        super.onDestroy();
    }

    @Override
    public void onRequest() {
        if (getCurrentFocus() != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    @Override
    public void onResponse(@Nullable Email email, boolean isSuccessful) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (!isSuccessful)
            Toast.makeText(ContactActivity.this, getString(R.string.string_avaliable), Toast.LENGTH_SHORT).show();
        else
            new DefaultDialog(ContactActivity.this)
                    .type(DefaultDialog.FINISH)
                    .setDesc(R.string.alert_send)
                    .start();
    }

    @Override
    public void onError(@Nullable Throwable t) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        Logger.show(t);
    }

    private boolean empty(@NonNull EditText editText) {
        if (!TextUtils.isEmpty(editText.getText())) return true;
        editText.setError(editText.getContext().getString(R.string.toast_inputs));
        editText.setFocusable(true);
        editText.requestFocus();
        return false;
    }

    private boolean email(@NonNull EditText editText) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(editText.getText()).matches()) return true;
        editText.setError(editText.getContext().getString(R.string.toast_inputs));
        editText.setFocusable(true);
        editText.requestFocus();
        return false;
    }

    private boolean empty(@NonNull EditText editText, int size) {
        if (!TextUtils.isEmpty(editText.getText()) && editText.getText().toString().length() >= size)
            return true;
        editText.setError(editText.getContext().getString(R.string.toast_inputs));
        editText.setFocusable(true);
        editText.requestFocus();
        return false;
    }

    /**
     * Processa o número de telefone falado, identificando o DDI e formatando corretamente
     *
     * @param spokenText Texto falado pelo usuário
     */
    private void processPhoneNumberWithDDI(String spokenText) {
        // Remove espaços extras e converte para minúsculas para facilitar a detecção
        String normalizedText = spokenText.toLowerCase().trim();

        // Extrai apenas os números do texto falado
        String numbersOnly = normalizedText.replaceAll("[^0-9]", "");

        // Lista de DDIs para verificar
        List<DDIInfo> ddiList = DDIInfo.getDDIList();
        DDIInfo detectedDDI = null;
        int ddiPosition = -1;
        String phoneNumber = numbersOnly;

        // Primeiro, verifica se o usuário mencionou o nome do país no texto falado
        for (int i = 0; i < ddiList.size(); i++) {
            DDIInfo ddi = ddiList.get(i);
            String countryName = ddi.country.toLowerCase();

            // Verifica se o usuário falou o nome do país
            if (normalizedText.contains(countryName)) {
                detectedDDI = ddi;
                ddiPosition = i;
                // Quando menciona o país, remove o DDI se estiver no início do número
                String ddiCode = ddi.code.replace("+", "");
                if (numbersOnly.startsWith(ddiCode)) {
                    phoneNumber = numbersOnly.substring(ddiCode.length());
                }
                break;
            }
        }

        // Se não mencionou o país, verifica se há indicação de DDI no texto (palavras como "mais", "plus", etc.)
        // ou se o número é muito longo para ser um número local brasileiro
        if (detectedDDI == null) {
            boolean hasDDIIndicator = normalizedText.contains("mais") ||
                    normalizedText.contains("more") ||
                    normalizedText.contains("dot") ||
                    normalizedText.contains("más") ||
                    normalizedText.contains("plus") ||
                    normalizedText.contains("+");

            // Apenas detecta DDI por código numérico se:
            // 1. Houver um indicador de DDI no texto falado OU
            // 2. O número for muito longo para ser brasileiro (mais de 13 dígitos - considerando DDI+DDD+número)
            if (hasDDIIndicator || numbersOnly.length() > 13) {
                for (int i = 0; i < ddiList.size(); i++) {
                    DDIInfo ddi = ddiList.get(i);
                    String ddiCode = ddi.code.replace("+", "");

                    // Verifica se o número começa com o código DDI
                    if (numbersOnly.startsWith(ddiCode)) {
                        detectedDDI = ddi;
                        ddiPosition = i;
                        phoneNumber = numbersOnly.substring(ddiCode.length());
                        break;
                    }
                }
            }
        }

        // Se não detectou DDI, usa +55 (Brasil) como padrão
        if (detectedDDI == null) {
            // Procura +55 na lista
            for (int i = 0; i < ddiList.size(); i++) {
                if (ddiList.get(i).code.equals("+55")) {
                    detectedDDI = ddiList.get(i);
                    ddiPosition = i;
                    break;
                }
            }
        }

        // Define o número de telefone no campo
        binding.edtPhone.setText(phoneNumber);
    }
}