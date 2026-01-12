package br.com.fivecom.litoralfm.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.models.social.Platform;
import br.com.fivecom.litoralfm.models.social.SocialFeedResponse;
import br.com.fivecom.litoralfm.models.social.SocialPost;

public class SocialFeedService {
    private static final String TAG = "SocialFeedService";

    public interface SocialFeedCallback {
        void onSuccess(SocialFeedResponse response);

        void onError(String error);
    }

    public static void fetchFeed(String apiUrl, SocialFeedCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String jsonString = response.toString();
                    Log.d(TAG, "JSON Response length: " + jsonString.length());

                    SocialFeedResponse feedResponse = parseJson(jsonString);

                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(feedResponse));

                } else {
                    String error = "HTTP Error: " + responseCode;
                    Log.e(TAG, error);
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(error));
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error fetching feed", e);
                String error = "Erro ao carregar feed: " + e.getMessage();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(error));
            }
        }).start();
    }

    private static SocialFeedResponse parseJson(String jsonString) throws Exception {
        JSONObject root = new JSONObject(jsonString);
        SocialFeedResponse response = new SocialFeedResponse();

        if (root.has("merged_at")) {
            response.setMergedAt(root.getLong("merged_at"));
        }

        if (root.has("schema")) {
            JSONArray schemaArray = root.getJSONArray("schema");
            List<String> schema = new ArrayList<>();
            for (int i = 0; i < schemaArray.length(); i++) {
                schema.add(schemaArray.getString(i));
            }
            response.setSchema(schema);
        }

        if (root.has("items")) {
            JSONArray itemsArray = root.getJSONArray("items");
            List<SocialPost> posts = new ArrayList<>();

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject itemObj = itemsArray.getJSONObject(i);
                SocialPost post = new SocialPost();

                if (itemObj.has("platform")) {
                    post.setPlatform(itemObj.getString("platform"));
                }

                if (itemObj.has("url")) {
                    post.setUrl(itemObj.getString("url"));
                }

                if (itemObj.has("text") && !itemObj.isNull("text")) {
                    post.setText(itemObj.getString("text"));
                }

                if (itemObj.has("posted_at")) {
                    post.setPostedAt(itemObj.getLong("posted_at"));
                }

                if (itemObj.has("media_urls")) {
                    JSONArray mediaArray = itemObj.getJSONArray("media_urls");
                    List<String> mediaUrls = new ArrayList<>();
                    for (int j = 0; j < mediaArray.length(); j++) {
                        mediaUrls.add(mediaArray.getString(j));
                    }
                    post.setMediaUrls(mediaUrls);
                }

                // Generate a robust unique ID
                int uniqueId;
                if (itemObj.has("id")) {
                    uniqueId = itemObj.getInt("id");
                } else {
                    // Fallback to generating a hash-based ID if server ID is missing
                    String key = post.getPlatform() + post.getUrl() + post.getPostedAt();
                    uniqueId = key.hashCode();
                }

                // Ensure uniqueness in the current list (handle very rare hash collisions or
                // duplicate server IDs)
                // We use a simple loop check for collisions since the list is relatively small
                // (hundreds).
                // For larger lists, a Set would be better to check against.
                final int originalId = uniqueId;
                int collisionCounter = 0;
                while (isIdTaken(posts, uniqueId)) {
                    uniqueId = originalId + (++collisionCounter);
                }

                post.setId(uniqueId);

                posts.add(post);
                Log.d(TAG, "Parsed post: " + post);
            }

            response.setItems(posts);
            Log.d(TAG, "Total posts parsed: " + posts.size());
        }

        return response;
    }

    private static boolean isIdTaken(List<SocialPost> posts, int id) {
        for (SocialPost p : posts) {
            if (p.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static void fetchFeedFiltered(String apiUrl, Platform platform, SocialFeedCallback callback) {
        fetchFeed(apiUrl, new SocialFeedCallback() {
            @Override
            public void onSuccess(SocialFeedResponse response) {
                List<SocialPost> filteredPosts = response.getFilteredItems(platform);

                SocialFeedResponse filteredResponse = new SocialFeedResponse();
                filteredResponse.setMergedAt(response.getMergedAt());
                filteredResponse.setSchema(response.getSchema());
                filteredResponse.setItems(filteredPosts);

                Log.d(TAG, "Filtered posts for " + platform + ": " + filteredPosts.size());
                callback.onSuccess(filteredResponse);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}
