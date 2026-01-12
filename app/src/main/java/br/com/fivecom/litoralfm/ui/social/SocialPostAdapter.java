package br.com.fivecom.litoralfm.ui.social;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.ArrayList;
import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.social.Platform;
import br.com.fivecom.litoralfm.models.social.SocialPost;

public class SocialPostAdapter extends RecyclerView.Adapter<SocialPostAdapter.ViewHolder> {

    private static final String PREFS_NAME = "social_posts_prefs";
    private static final String KEY_LIKES = "likes_";
    private static final String KEY_BOOKMARKS = "bookmarks_";
    private Context context;
    private List<SocialPost> posts;
    private SharedPreferences prefs;

    // Controle para garantir que apenas um vídeo toque por vez
    private static ViewHolder currentlyPlayingVideo = null;

    private java.util.Set<Integer> hiddenPostIds = new java.util.HashSet<>();
    private OnHiddenPostsChangedListener hiddenPostsListener;

    public interface OnHiddenPostsChangedListener {
        void onHiddenPostsChanged(int count);
    }

    public void setOnHiddenPostsChangedListener(OnHiddenPostsChangedListener listener) {
        this.hiddenPostsListener = listener;
    }

    public void clearHiddenPosts() {
        hiddenPostIds.clear();
        if (hiddenPostsListener != null) {
            hiddenPostsListener.onHiddenPostsChanged(0);
        }
        notifyDataSetChanged();
    }

    public SocialPostAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setPosts(List<SocialPost> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPosts(List<SocialPost> newPosts) {
        if (newPosts != null && !newPosts.isEmpty()) {
            int startPosition = this.posts.size();
            this.posts.addAll(newPosts);
            notifyItemRangeInserted(startPosition, newPosts.size());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                R.layout.item_social_post,
                parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SocialPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Pausa o vídeo quando o ViewHolder é reciclado
        holder.pauseVideo();
        holder.currentVideoUrl = null;
    }

    /**
     * Pausa o vídeo que está tocando atualmente
     * Pode ser chamado externamente se necessário
     */
    public void pauseCurrentVideo() {
        if (currentlyPlayingVideo != null) {
            currentlyPlayingVideo.pauseVideo();
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    /**
     * Verifica se a URL é um vídeo baseado na extensão
     */
    private boolean isVideoUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        String lowerUrl = url.toLowerCase();
        return lowerUrl.endsWith(".mp4") ||
                lowerUrl.endsWith(".mov") ||
                lowerUrl.endsWith(".webm") ||
                lowerUrl.endsWith(".3gp") ||
                lowerUrl.endsWith(".mkv") ||
                lowerUrl.contains("/video/") ||
                lowerUrl.contains("video=true");
    }

    private String findVideoUrl(List<String> urls) {
        if (urls == null)
            return null;
        for (String url : urls) {
            if (isVideoUrl(url)) {
                return url;
            }
        }
        return null;
    }

    private boolean isLiked(int postId) {
        return prefs.getBoolean(KEY_LIKES + postId, false);
    }

    private void setLiked(int postId, boolean liked) {
        prefs.edit().putBoolean(KEY_LIKES + postId, liked).apply();
    }

    private boolean isBookmarked(int postId) {
        return prefs.getBoolean(KEY_BOOKMARKS + postId, false);
    }

    private void setBookmarked(int postId, boolean bookmarked) {
        prefs.edit().putBoolean(KEY_BOOKMARKS + postId, bookmarked).apply();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView hiddenPlatformIcon;
        View contentView;
        View hiddenView;
        ImageView postImage;
        WebView postVideoWebView;
        ImageView videoPlayIndicator;
        ImageView platformIcon;
        TextView postText;
        TextView postDate;
        TextView mediaCounter;
        TextView redeName;
        View rootView;
        ImageView btnLike;
        ImageView btnBookmark;
        ImageView btnShare;
        ImageView btnExpand;

        String currentVideoUrl = null;
        boolean isVideoPlaying = false;

        // Tracking hidden state directly in the viewholder is tricky with recycling,
        // ideally should be in the adapter or model.
        // For simplicity, we'll track in a HashSet in the Adapter.

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            contentView = itemView.findViewById(R.id.content_view);
            hiddenView = itemView.findViewById(R.id.hidden_view);
            hiddenPlatformIcon = itemView.findViewById(R.id.hidden_platform_icon);

            postImage = itemView.findViewById(R.id.post_image);
            postVideoWebView = itemView.findViewById(R.id.post_video_webview);
            videoPlayIndicator = itemView.findViewById(R.id.video_play_indicator);
            platformIcon = itemView.findViewById(R.id.platform_icon);
            postText = itemView.findViewById(R.id.post_text);
            postDate = itemView.findViewById(R.id.post_date);
            mediaCounter = itemView.findViewById(R.id.media_counter);
            redeName = itemView.findViewById(R.id.rede_name);
            rootView = itemView;

            btnLike = itemView.findViewById(R.id.btn_like);
            btnBookmark = itemView.findViewById(R.id.btn_bookmark);
            btnShare = itemView.findViewById(R.id.btn_share);
            btnExpand = itemView.findViewById(R.id.bt_expand);

            // Configura a WebView para vídeo
            setupVideoWebView();
        }

        @SuppressLint("SetJavaScriptEnabled")
        private void setupVideoWebView() {
            if (postVideoWebView == null)
                return;

            WebSettings settings = postVideoWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setMediaPlaybackRequiresUserGesture(false);
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
            settings.setAllowFileAccess(true);
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);

            postVideoWebView.setWebViewClient(new WebViewClient());
            postVideoWebView.setWebChromeClient(new WebChromeClient());
        }

        void bind(SocialPost post) {
            Log.d("SocialPostAdapter",
                    "Binding post ID: " + post.getId() + " hidden: " + hiddenPostIds.contains(post.getId()));
            // Limpa estados anteriores
            postImage.setVisibility(View.GONE);
            postVideoWebView.setVisibility(View.GONE);
            videoPlayIndicator.setVisibility(View.GONE);
            currentVideoUrl = null;
            isVideoPlaying = false;

            // Configura visibilidade baseado no estado oculto
            boolean isHidden = hiddenPostIds.contains(post.getId());
            if (isHidden) {
                if (contentView != null)
                    contentView.setVisibility(View.GONE);
                if (hiddenView != null)
                    hiddenView.setVisibility(View.VISIBLE);
            } else {
                if (contentView != null) {
                    contentView.setVisibility(View.VISIBLE);
                    contentView.setAlpha(1f); // Reset alpha for recycled views
                }
                if (hiddenView != null) {
                    hiddenView.setVisibility(View.GONE);
                    hiddenView.setAlpha(1f); // Reset alpha
                }
            }

            // Configura a visualização "Oculto"
            if (hiddenPlatformIcon != null) {
                int iconResId = getPlatformIcon(post.getPlatformEnum());
                if (iconResId != 0) {
                    hiddenPlatformIcon.setImageResource(iconResId);
                } else {
                    hiddenPlatformIcon.setImageResource(R.drawable.ic_facebook); // Fallback
                }
            }

            // Long click para ocultar
            if (contentView != null) {
                contentView.setOnLongClickListener(v -> {
                    // Oculta o post
                    hiddenPostIds.add(post.getId());
                    if (hiddenPostsListener != null) {
                        hiddenPostsListener.onHiddenPostsChanged(hiddenPostIds.size());
                    }

                    // Animação simples de troca
                    contentView.setVisibility(View.GONE);
                    if (hiddenView != null) {
                        hiddenView.setVisibility(View.VISIBLE);
                        hiddenView.setAlpha(0f);
                        hiddenView.animate().alpha(1f).setDuration(300).start();
                    }

                    return true;
                });
            }

            // Long click para mostrar
            if (hiddenView != null) {
                hiddenView.setOnLongClickListener(v -> {
                    // Mostra o post
                    hiddenPostIds.remove(post.getId());
                    if (hiddenPostsListener != null) {
                        hiddenPostsListener.onHiddenPostsChanged(hiddenPostIds.size());
                    }

                    hiddenView.setVisibility(View.GONE);
                    if (contentView != null) {
                        contentView.setVisibility(View.VISIBLE);
                        contentView.setAlpha(0f);
                        contentView.animate().alpha(1f).setDuration(300).start();
                    }

                    return true;
                });
            }

            // Pausa este vídeo se não for o que está tocando
            if (postVideoWebView != null && currentlyPlayingVideo != this) {
                pauseVideo();
            }

            if (post.getText() != null && !post.getText().isEmpty()) {
                postText.setText(post.getText());
                postText.setVisibility(View.VISIBLE);
            } else {
                postText.setVisibility(View.GONE);
            }

            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    post.getPostedAt() * 1000,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
            postDate.setText(relativeTime);

            // Define o nome da rede social e o ícone
            Platform platformEnum = post.getPlatformEnum();
            if (platformEnum != null) {
                // Define o nome da rede social
                if (redeName != null) {
                    redeName.setText(platformEnum.getDisplayName());
                }

                // Define o ícone da plataforma
                int iconResId = getPlatformIcon(platformEnum);
                if (iconResId != 0) {
                    platformIcon.setImageResource(iconResId);
                }
            } else {
                // Fallback se a plataforma não for reconhecida
                if (redeName != null) {
                    String platformName = post.getPlatform();
                    if (platformName != null && !platformName.isEmpty()) {
                        // Capitaliza a primeira letra
                        String displayName = platformName.substring(0, 1).toUpperCase() +
                                platformName.substring(1).toLowerCase();
                        redeName.setText(displayName);
                    } else {
                        redeName.setText("Rede Social");
                    }
                }
            }

            // Configura mídia (imagem ou vídeo)
            // Configura mídia (imagem ou vídeo)
            if (post.hasMedia()) {
                List<String> mediaUrls = post.getMediaUrls();
                String videoUrl = findVideoUrl(mediaUrls);
                String displayUrl = post.getFirstMediaUrl(); // Default to first for thumbnail

                if (videoUrl != null) {
                    // Configura vídeo
                    setupVideo(videoUrl, displayUrl);
                } else {
                    // Configura imagem
                    setupImage(displayUrl);
                }

                // Contador de mídias
                if (post.getMediaCount() > 1) {
                    mediaCounter.setText("+" + (post.getMediaCount() - 1));
                    mediaCounter.setVisibility(View.VISIBLE);
                } else {
                    mediaCounter.setVisibility(View.GONE);
                }
            } else {
                postImage.setVisibility(View.GONE);
                postVideoWebView.setVisibility(View.GONE);
                videoPlayIndicator.setVisibility(View.GONE);
                mediaCounter.setVisibility(View.GONE);
            }

            if (btnLike != null) {
                boolean isLiked = isLiked(post.getId());
                updateLikeButton(btnLike, isLiked);

                btnLike.setOnClickListener(v -> {
                    boolean currentlyLiked = isLiked(post.getId());
                    boolean newLikedState = !currentlyLiked;

                    setLiked(post.getId(), newLikedState);

                    updateLikeButton(btnLike, newLikedState);

                    if (newLikedState) {
                        Toast.makeText(context, "❤️ Curtiu o post!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "💔 Descurtiu o post", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnBookmark != null) {
                boolean isBookmarked = isBookmarked(post.getId());
                updateBookmarkButton(btnBookmark, isBookmarked);

                btnBookmark.setOnClickListener(v -> {
                    boolean currentlyBookmarked = isBookmarked(post.getId());
                    boolean newBookmarkedState = !currentlyBookmarked;

                    setBookmarked(post.getId(), newBookmarkedState);

                    updateBookmarkButton(btnBookmark, newBookmarkedState);

                    if (newBookmarkedState) {
                        Toast.makeText(context, "🔖 Post salvo nos favoritos!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "📂 Post removido dos favoritos", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    if (post.getUrl() != null && !post.getUrl().isEmpty()) {
                        String shareText = post.getText() != null && !post.getText().isEmpty()
                                ? post.getText() + "\n\n" + post.getUrl()
                                : post.getUrl();

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Confira este post!");

                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
                    } else {
                        Toast.makeText(context, "Não é possível compartilhar este post", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (btnExpand != null) {
                btnExpand.setOnClickListener(v -> {
                    String url = post.getUrl();
                    if (url != null && !url.isEmpty()) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(context, "Não foi possível abrir o link", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Link indisponível", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            rootView.setOnClickListener(v -> {
                if (post.getUrl() != null && !post.getUrl().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(post.getUrl()));
                    context.startActivity(intent);
                }
            });
        }

        private void setupImage(String imageUrl) {
            postImage.setVisibility(View.VISIBLE);
            postVideoWebView.setVisibility(View.GONE);
            videoPlayIndicator.setVisibility(View.GONE);
            currentVideoUrl = null;
            isVideoPlaying = false;

            int radiusPx = (int) (12 * context.getResources().getDisplayMetrics().density);

            Glide.with(context)
                    .load(imageUrl)
                    .transform(new RoundedCorners(radiusPx))
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .into(postImage);

            // Remove listener de vídeo da imagem
            postImage.setOnClickListener(null);
        }

        private void setupVideo(String videoUrl, String thumbnailUrl) {
            postImage.setVisibility(View.VISIBLE);
            postVideoWebView.setVisibility(View.GONE);
            videoPlayIndicator.setVisibility(View.VISIBLE);
            currentVideoUrl = videoUrl;
            isVideoPlaying = false;

            // Tenta carregar uma thumbnail do vídeo usando Glide
            int radiusPx = (int) (12 * context.getResources().getDisplayMetrics().density);

            // Se for nulo ou vazio, usa o próprio vídeo para gerar thumbnail
            String loadUrl = (thumbnailUrl != null && !thumbnailUrl.isEmpty()) ? thumbnailUrl : videoUrl;

            Glide.with(context)
                    .load(loadUrl) // Glide pode tentar extrair frame do vídeo
                    .transform(new RoundedCorners(radiusPx))
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .into(postImage);

            // Listener para reproduzir vídeo na WebView
            View.OnClickListener videoClickListener = v -> {
                if (currentVideoUrl != null && !currentVideoUrl.isEmpty()) {
                    playVideoInWebView(currentVideoUrl);
                }
            };

            postImage.setOnClickListener(videoClickListener);
            if (videoPlayIndicator != null) {
                videoPlayIndicator.setOnClickListener(videoClickListener);
            }
        }

        private void playVideoInWebView(String videoUrl) {
            if (postVideoWebView == null || videoUrl == null || videoUrl.isEmpty()) {
                return;
            }

            // Pausa o vídeo que está tocando atualmente (se houver)
            if (currentlyPlayingVideo != null && currentlyPlayingVideo != this) {
                currentlyPlayingVideo.pauseVideo();
            }

            // Configura este como o vídeo atual
            currentlyPlayingVideo = this;
            isVideoPlaying = true;

            // Oculta a imagem e mostra a WebView
            postImage.setVisibility(View.GONE);
            postVideoWebView.setVisibility(View.VISIBLE);
            videoPlayIndicator.setVisibility(View.GONE);

            // Cria HTML para reproduzir o vídeo
            String html = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "    <style>" +
                    "        * { margin: 0; padding: 0; }" +
                    "        body { background: #000; display: flex; align-items: center; justify-content: center; height: 100vh; }"
                    +
                    "        video { width: 100%; height: 100%; object-fit: contain; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <video controls autoplay>" +
                    "        <source src=\"" + videoUrl + "\" type=\"video/mp4\">" +
                    "        Seu navegador não suporta vídeo HTML5." +
                    "    </video>" +
                    "</body>" +
                    "</html>";

            postVideoWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            postVideoWebView.onResume();

            Log.d("SocialPostAdapter", "▶️ Reproduzindo vídeo: " + videoUrl);
        }

        private void pauseVideo() {
            if (postVideoWebView != null && isVideoPlaying) {
                postVideoWebView.onPause();
                postVideoWebView.loadUrl("about:blank");
                postVideoWebView.setVisibility(View.GONE);
                postImage.setVisibility(View.VISIBLE);
                videoPlayIndicator.setVisibility(View.VISIBLE);
                isVideoPlaying = false;

                if (currentlyPlayingVideo == this) {
                    currentlyPlayingVideo = null;
                }

                Log.d("SocialPostAdapter", "⏸️ Vídeo pausado");
            }
        }

        private void updateLikeButton(ImageView button, boolean isLiked) {
            if (isLiked) {
                button.setImageResource(R.drawable.ic_heart_filled);
            } else {
                button.setImageResource(R.drawable.ic_heart_not_filled);
            }
        }

        private void updateBookmarkButton(ImageView button, boolean isBookmarked) {
            if (isBookmarked) {
                button.setImageResource(R.drawable.ic_bookmark_filled);
            } else {
                button.setImageResource(R.drawable.ic_bookmark_not_filled);
            }
        }
    }

    /**
     * Retorna o recurso do ícone para a plataforma especificada
     * Retorna 0 se o ícone não existir
     */
    private int getPlatformIcon(Platform platform) {
        if (platform == null)
            return 0;

        String iconName;
        switch (platform) {
            case facebook:
                iconName = "ic_facebook";
                break;
            case instagram:
                iconName = "ic_instagram";
                break;
            case youtube:
                iconName = "ic_youtube";
                break;
            case x:
                iconName = "ic_x";
                break;
            default:
                return 0;
        }

        // Verifica se o recurso existe
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(iconName, "drawable", context.getPackageName());

        // Se não encontrar, tenta fallback
        if (resId == 0) {
            // Fallback para ícones conhecidos
            if (platform == Platform.youtube || platform == Platform.x) {
                resId = R.drawable.ic_facebook; // Usa Facebook como fallback
            }
        }

        return resId;
    }
}
