package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import br.com.fivecom.litoralfm.models.scheduler.Programa;
import br.com.fivecom.litoralfm.models.scheduler.ProgramacaoResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Serviço para buscar programação da API legada
 * Convertido de ProgramacaoService.swift
 */
public class ProgramacaoService {

    private static final String TAG = "ProgramacaoService";
    private static final String API_URL = "https://radiointegration.ticketss.app/litoralfm/programacao.json";
    private static final long REFRESH_INTERVAL_MS = 5 * 60 * 1000; // 5 minutos

    private final OkHttpClient client;
    private final Gson gson;
    private final Handler handler;
    private final Runnable refreshRunnable;
    
    private ProgramacaoResponse programacao;
    private Programa programaAtual;
    private boolean isLoading;
    private String errorMessage;
    private ProgramacaoCallback callback;

    public interface ProgramacaoCallback {
        void onProgramaAtualChanged(Programa programa);
        void onLoadingChanged(boolean isLoading);
        void onError(String errorMessage);
    }

    public ProgramacaoService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.handler = new Handler(Looper.getMainLooper());
        
        this.refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchProgramacao();
                handler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
    }

    public void setCallback(ProgramacaoCallback callback) {
        this.callback = callback;
    }

    public void start() {
        fetchProgramacao();
        handler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    public void stop() {
        handler.removeCallbacks(refreshRunnable);
    }

    public void refresh() {
        fetchProgramacao();
    }

    public Programa getProgramaAtual() {
        return programaAtual;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void fetchProgramacao() {
        isLoading = true;
        errorMessage = null;
        
        if (callback != null) {
            callback.onLoadingChanged(true);
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(() -> {
                    isLoading = false;
                    errorMessage = "Erro ao carregar: " + e.getLocalizedMessage();
                    Log.e(TAG, "Erro ao carregar programação", e);
                    
                    if (callback != null) {
                        callback.onLoadingChanged(false);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handler.post(() -> {
                    isLoading = false;
                    
                    if (callback != null) {
                        callback.onLoadingChanged(false);
                    }

                    if (!response.isSuccessful()) {
                        errorMessage = "Erro na resposta: " + response.code();
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                        return;
                    }

                    try {
                        String responseBody = response.body() != null ? response.body().string() : null;
                        if (responseBody == null || responseBody.isEmpty()) {
                            errorMessage = "Sem dados recebidos";
                            if (callback != null) {
                                callback.onError(errorMessage);
                            }
                            return;
                        }

                        programacao = gson.fromJson(responseBody, ProgramacaoResponse.class);
                        atualizarProgramaAtual();
                    } catch (Exception e) {
                        errorMessage = "Erro ao decodificar: " + e.getLocalizedMessage();
                        Log.e(TAG, "Erro de decodificação", e);
                        if (callback != null) {
                            callback.onError(errorMessage);
                        }
                    }
                });
            }
        });
    }

    private void atualizarProgramaAtual() {
        if (programacao == null || programacao.getDiasSemana() == null) {
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int diaSemana = calendar.get(Calendar.DAY_OF_WEEK);
        int horaAtual = calendar.get(Calendar.HOUR_OF_DAY);
        int minutoAtual = calendar.get(Calendar.MINUTE);

        List<Programa> programasDoDia = programacao.getDiasSemana().programasParaDia(diaSemana);
        
        if (programasDoDia.isEmpty()) {
            return;
        }

        Programa programaEncontrado = null;

        for (int index = 0; index < programasDoDia.size(); index++) {
            Programa programa = programasDoDia.get(index);
            
            // Remove "h" e converte para hora e minuto
            String horarioString = programa.getHorario().replace("h", ":");
            String[] componentes = horarioString.split(":");

            int horarioPrograma;
            try {
                horarioPrograma = Integer.parseInt(componentes[0].trim());
            } catch (NumberFormatException e) {
                continue;
            }
            
            int minutoPrograma = componentes.length > 1 ? 
                    parseIntSafe(componentes[1].trim(), 0) : 0;

            // Converte hora e minuto para minutos totais do dia para comparação
            int minutosPrograma = horarioPrograma * 60 + minutoPrograma;
            int minutosAtuais = horaAtual * 60 + minutoAtual;

            // Verifica se é o último programa do dia
            if (index == programasDoDia.size() - 1) {
                if (minutosAtuais >= minutosPrograma) {
                    programaEncontrado = programa;
                    break;
                }
            } else {
                // Verifica o próximo programa para determinar o intervalo
                Programa proximoPrograma = programasDoDia.get(index + 1);
                String proximoHorarioString = proximoPrograma.getHorario().replace("h", ":");
                String[] proximosComponentes = proximoHorarioString.split(":");

                int proximoHorario;
                try {
                    proximoHorario = Integer.parseInt(proximosComponentes[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                
                int proximoMinuto = proximosComponentes.length > 1 ? 
                        parseIntSafe(proximosComponentes[1].trim(), 0) : 0;
                int proximosMinutos = proximoHorario * 60 + proximoMinuto;

                if (minutosAtuais >= minutosPrograma && minutosAtuais < proximosMinutos) {
                    programaEncontrado = programa;
                    break;
                }
            }
        }

        // Se não encontrou programa (ex: madrugada), pega o primeiro programa do dia
        if (programaEncontrado == null && !programasDoDia.isEmpty()) {
            programaEncontrado = programasDoDia.get(0);
        }

        this.programaAtual = programaEncontrado;
        
        if (callback != null) {
            callback.onProgramaAtualChanged(programaAtual);
        }
    }

    private int parseIntSafe(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
