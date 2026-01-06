package br.com.fivecom.litoralfm.utils.requests;

import static br.com.fivecom.litoralfm.utils.constants.Constants.ID;
import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import okhttp3.Call;
import okhttp3.Request;
import br.com.fivecom.litoralfm.models.email.Email;
import br.com.fivecom.litoralfm.models.promotion.Promotion;
import br.com.fivecom.litoralfm.models.promotion.Promotions;
import br.com.fivecom.litoralfm.models.weather.Weather;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestManager {

    public enum RequestType {
        CHAT,
        PROMOTION,
        PROMOTIONS,
        SCHEDULERS,
        OPEN_METEO,
        OPEN_WEATHER,
        WEATHER,
        LYRIC,
        EMAIL,
        YOUTUBE_ID,
        YOUTUBE,
        ENQUETE,
        ENQUETE_RESPOSTA,
        HOROSCOPE,
        QUIZ
    }

    private Map<RequestType, List<Call>> mapOkCall;
    private Map<RequestType, List<retrofit2.Call>> mapRetroCall;

    private void add(RequestType type, retrofit2.Call call) {
        if (mapRetroCall == null)
            mapRetroCall = new HashMap<>();
        if (!mapRetroCall.containsKey(type))
            mapRetroCall.put(type, new ArrayList<>());
        mapRetroCall.get(type).add(call);
    }

    private void add(RequestType type, Call call) {
        if (mapOkCall == null)
            mapOkCall = new HashMap<>();
        if (!mapOkCall.containsKey(type))
            mapOkCall.put(type, new ArrayList<>());
        mapOkCall.get(type).add(call);
    }

    private void remove(RequestType type, Call call) {
        if (mapOkCall != null && mapOkCall.containsKey(type))
            mapOkCall.get(type).remove(call);
    }

    private void remove(RequestType type, retrofit2.Call call) {
        if (mapRetroCall != null && mapRetroCall.containsKey(type))
            mapRetroCall.get(type).remove(call);
    }

    public void fetchChat(@NonNull String url, @NonNull String prompt, @NonNull RequestListener<String> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.chat(prompt, data.radios.get(ID).name);
        add(RequestType.CHAT, retroCall);
        retroCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<String> call, @NonNull Response<String> response) {
                requestListener.onResponse(response.body(), response.isSuccessful());
                remove(RequestType.CHAT, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<String> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.CHAT, retroCall);
            }
        });
    }


    public void fetchPromotion(@NonNull String url, @NonNull String radio_id, @NonNull String modelo, @NonNull RequestListener<Promotion> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.promotions(radio_id, modelo);
        add(RequestType.PROMOTION, retroCall);
        retroCall.enqueue(new Callback<Promotions>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Promotions> call, @NonNull Response<Promotions> response) {
                Promotions promotion = response.body();
                boolean isEmpty = promotion == null || promotion.promocoes == null || promotion.promocoes.isEmpty();
                requestListener.onResponse(isEmpty ? null : promotion.promocoes.get(0),
                        response.isSuccessful() && !isEmpty);
                remove(RequestType.PROMOTION, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Promotions> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.PROMOTION, retroCall);
            }
        });
    }

    @OptIn(markerClass = UnstableApi.class)
    public void fetchPromotions(@NonNull String url, @NonNull String radio_id, @Nullable String status, @NonNull String modelo, @NonNull RequestListener<List<Promotion>> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.promotions(radio_id, modelo);
        add(RequestType.PROMOTIONS, retroCall);
        retroCall.enqueue(new Callback<Promotions>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Promotions> call, @NonNull Response<Promotions> response) {
                Promotions promotion = response.body();
                boolean isEmpty = promotion == null || promotion.promocoes == null || promotion.promocoes.isEmpty();
                if (!isEmpty && response.isSuccessful()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && status != null) {
                        promotion.promocoes.removeIf(item -> !status.equalsIgnoreCase(item.status));
                    } else if (status != null) {
                        List<Promotion> filtrados = new ArrayList<>();
                        for (Promotion item : promotion.promocoes)
                            if (status.equals(item.status))
                                filtrados.add(item);
                        promotion.promocoes = filtrados;
                    }
                    requestListener.onResponse(promotion.promocoes, true);
                } else requestListener.onResponse(null, false);
                remove(RequestType.PROMOTIONS, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Promotions> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.PROMOTIONS, retroCall);
            }
        });
    }

    // Comentado - classes Scheduler/Schedulers não existem
    /*public void fetchSchedulers(@NonNull String url, @NonNull String radio_id, @Nullable String day, @NonNull RequestListener<List<Scheduler>> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.schedulers(radio_id, day);
        add(RequestType.SCHEDULERS, retroCall);
        retroCall.enqueue(new Callback<Schedulers>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Schedulers> call, @NonNull Response<Schedulers> response) {
                Schedulers schedulers = response.body();
                boolean isEmpty = schedulers == null || schedulers.schedules == null || schedulers.schedules.isEmpty();
                requestListener.onResponse(isEmpty ? null : schedulers.schedules, response.isSuccessful() && !isEmpty);
                remove(RequestType.SCHEDULERS, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Schedulers> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.SCHEDULERS, retroCall);
            }
        });
    }

    public void fetchSchedulers(@NonNull String url, @NonNull String radio_id, @NonNull RequestListener<List<Scheduler>> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.schedulers(radio_id);
        add(RequestType.SCHEDULERS, retroCall);
        retroCall.enqueue(new Callback<Schedulers>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Schedulers> call, @NonNull Response<Schedulers> response) {
                Schedulers schedulers = response.body();
                boolean isEmpty = schedulers == null || schedulers.schedules == null || schedulers.schedules.isEmpty();
                requestListener.onResponse(isEmpty ? null : schedulers.schedules, response.isSuccessful() && !isEmpty);
                remove(RequestType.SCHEDULERS, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Schedulers> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.SCHEDULERS, retroCall);
            }
        });
    }

    public void fetchSchedulers(@NonNull String url, @NonNull String radio_id, boolean isNow, @NonNull RequestListener<List<Scheduler>> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = isNow ? task.schedulerNow(radio_id) : task.scheduler(radio_id);
        add(RequestType.SCHEDULERS, retroCall);
        retroCall.enqueue(new Callback<Schedulers>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Schedulers> call, @NonNull Response<Schedulers> response) {
                Schedulers schedulers = response.body();
                boolean isEmpty = schedulers == null || schedulers.schedules == null || schedulers.schedules.isEmpty();
                requestListener.onResponse(isEmpty ? null : schedulers.schedules, response.isSuccessful() && !isEmpty);
                remove(RequestType.SCHEDULERS, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Schedulers> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.SCHEDULERS, retroCall);
            }
        });
    }*/

    public void fetchWeather(@NonNull String url, @NonNull Double latitude, @NonNull Double longitude, @NonNull RequestListener<Weather> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.weather(latitude, longitude);
        add(RequestType.WEATHER, retroCall);
        retroCall.enqueue(new Callback<Weather>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Weather> call, @NonNull Response<Weather> response) {
                Weather string = response.body();
                requestListener.onResponse(string, response.isSuccessful() && string != null);
                remove(RequestType.WEATHER, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Weather> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.WEATHER, retroCall);
            }
        });
    }

    public void fetchEmail(@NonNull RequestListener<Email> requestListener, int id_option, String url, String... strings) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call<Email> retroCall;
        switch (id_option) {
            default:
                retroCall = task.email(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8]);
                break;
            case 1:
                retroCall = task.support(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 2:
                retroCall = task.music(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 3:
                retroCall = task.advertising(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 4:
                retroCall = task.email_promotion(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8], strings[9], strings[10], strings[11]);
                break;
            case 5:
                retroCall = task.prayer(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 6:
                retroCall = task.testimony(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 7:
                retroCall = task.suggestion(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7]);
                break;
            case 8:
                retroCall = task.quiz(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7], strings[8], strings[9]);
                break;
        }
        add(RequestType.EMAIL, retroCall);
        retroCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Email> call, @NonNull Response<Email> response) {
                Email email = response.body();
                requestListener.onResponse(email, response.isSuccessful() && email != null && email.erro == 0);
                remove(RequestType.EMAIL, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Email> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                remove(RequestType.EMAIL, retroCall);
                cancel();
            }
        });
    }

    public void fetchYoutubeId(@NonNull String url, @NonNull String artist, @NonNull String music, @NonNull RequestListener<String> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call retroCall = task.youtubeId(artist, music);
        add(RequestType.YOUTUBE_ID, retroCall);
        retroCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<String> call, @NonNull Response<String> response) {
                String item = response.body();
                requestListener.onResponse(item == null || item.isEmpty() ? null : item, response.isSuccessful());
                remove(RequestType.YOUTUBE_ID, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<String> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                cancel();
                remove(RequestType.YOUTUBE_ID, retroCall);
            }
        });
    }

    public void fetchYoutube(@NonNull String url, @NonNull RequestListener<String> requestListener) {
        requestListener.onRequest();
        Call okCall = new HttpClient().okhttp().newCall(new Request.Builder().url(url).get().build());
        add(RequestType.YOUTUBE, okCall);
        okCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requestListener.onError(e);
                cancel();
                remove(RequestType.YOUTUBE, okCall);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                Document document = getDomElement(response.body().string());
                if (document == null)
                    requestListener.onResponse(null, false);
                else {
                    NodeList nl = document.getElementsByTagName("entry");
                    Element e = (Element) nl.item(0);
                    String value = getValue(e, "yt:videoId").replace("yt:video:", "");
                    requestListener.onResponse(value, true);
                }
                remove(RequestType.YOUTUBE, okCall);
            }
        });
    }

    public void submitEnqueteResposta(@NonNull String url, @NonNull String pergunta, @NonNull String opcao,
                                      @NonNull String sistema, @NonNull String dispositivo,
                                      @NonNull RequestListener<Email> requestListener) {
        requestListener.onRequest();
        TasksAPI task = new HttpClient().retrofit(url).create(TasksAPI.class);
        retrofit2.Call<Email> retroCall = task.enqueteResposta(pergunta, opcao, sistema, dispositivo);
        add(RequestType.ENQUETE_RESPOSTA, retroCall);
        retroCall.enqueue(new Callback<Email>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Email> call, @NonNull Response<Email> response) {
                Email email = response.body();
                requestListener.onResponse(email, response.isSuccessful() && email != null && email.erro == 0);
                remove(RequestType.ENQUETE_RESPOSTA, retroCall);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Email> call, @NonNull Throwable throwable) {
                requestListener.onError(throwable);
                remove(RequestType.ENQUETE_RESPOSTA, retroCall);
                cancel();
            }
        });
    }

    public void cancel() {
        if (mapOkCall != null) {
            for (List<Call> calls : mapOkCall.values()) {
                for (Call okCall : calls)
                    okCall.cancel();
            }
            mapOkCall.clear();
        }
        if (mapRetroCall != null) {
            for (List<retrofit2.Call> calls : mapRetroCall.values()) {
                for (retrofit2.Call retroCall : calls)
                    retroCall.cancel();
            }
            mapRetroCall.clear();
        }
    }

    public void cancel(RequestType... types) {
        if (types == null || types.length == 0) {
            cancel();
            return;
        }

        for (RequestType type : types) {
            if (mapOkCall != null && mapOkCall.containsKey(type)) {
                List<Call> calls = mapOkCall.get(type);
                for (Call okCall : calls)
                    okCall.cancel();
                mapOkCall.remove(type);
            }
            if (mapRetroCall != null && mapRetroCall.containsKey(type)) {
                List<retrofit2.Call> calls = mapRetroCall.get(type);
                for (retrofit2.Call retroCall : calls)
                    retroCall.cancel();
                mapRetroCall.remove(type);
            }
        }
    }

    @Nullable
    private List<Integer> extractYears(@Nullable List<String> rawPaths) {
        if (rawPaths == null) return null;
        List<Integer> years = new ArrayList<>();
        for (String path : rawPaths) {
            Integer year = extractYear(path);
            if (year != null && !years.contains(year))
                years.add(year);
        }
        Collections.sort(years);
        return years;
    }

    @Nullable
    private List<String> extractMonths(@Nullable List<String> rawPaths) {
        if (rawPaths == null) return null;
        List<String> months = new ArrayList<>();
        for (String path : rawPaths) {
            String month = extractMonth(path);
            if (!TextUtils.isEmpty(month) && !months.contains(month))
                months.add(month);
        }
        return months;
    }

    @Nullable
    private Integer extractYear(@Nullable String path) {
        if (TextUtils.isEmpty(path)) return null;
        String normalized = normalizePath(path);
        if (TextUtils.isEmpty(normalized)) return null;
        String[] parts = normalized.split("/");
        if (parts.length == 0) return null;
        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Nullable
    private String extractMonth(@Nullable String path) {
        if (TextUtils.isEmpty(path)) return null;
        String normalized = normalizePath(path);
        if (TextUtils.isEmpty(normalized)) return null;
        String[] parts = normalized.split("/");
        if (parts.length < 2) return null;
        String fileName = parts[parts.length - 1];
        if (fileName.endsWith(".json"))
            fileName = fileName.substring(0, fileName.length() - 5);
        return fileName;
    }

    @NonNull
    private String normalizePath(@NonNull String path) {
        String trimmed = path.trim();
        while (trimmed.startsWith("/"))
            trimmed = trimmed.substring(1);
        while (trimmed.endsWith("/"))
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        return trimmed;
    }

    private Document getDomElement(@Nullable String xml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            return db.parse(is);
        } catch (Exception e) {
            return null;
        }
    }

    private String getElementValue(@Nullable Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    private String getValue(@NonNull Element item, @NonNull String str) {
        NodeList n = item.getElementsByTagName(str);
        return this.getElementValue(n.item(0));
    }

}
