package br.com.fivecom.litoralfm.ui.social;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.models.social.Platform;
import br.com.fivecom.litoralfm.models.social.SocialFeedResponse;
import br.com.fivecom.litoralfm.models.social.SocialPost;
import br.com.fivecom.litoralfm.services.SocialFeedService;
import br.com.fivecom.litoralfm.ui.main.MainActivity;

public class SocialFeedFragment extends Fragment {

    private static final String ARG_API_URL = "api_url";
    private static final String ARG_PLATFORM = "platform";
    private static final String PREFS_NAME = "social_posts_prefs";
    private static final String KEY_LIKES = "likes_";
    private static final String KEY_BOOKMARKS = "bookmarks_";
    private String apiUrl;
    private Platform platform;
    private RecyclerView recyclerView;
    private SocialPostAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefresh;
    private EditText searchBar;
    private ChipGroup chipGroupFilters;
    private List<SocialPost> allPosts = new ArrayList<>();
    private String currentSearch = "";

    // Múltiplas plataformas selecionadas
    private Set<Platform> selectedPlatforms = new HashSet<>();
    private boolean showOnlyLiked = false;
    private boolean showOnlySaved = false;

    // Referências aos chips para atualização visual
    private Chip chipTodos;
    private Chip chipLiked;
    private Chip chipSaved;
    private List<Chip> platformChips = new ArrayList<>();

    public static SocialFeedFragment newInstance(String apiUrl, Platform platform) {
        SocialFeedFragment fragment = new SocialFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_API_URL, apiUrl);
        args.putSerializable(ARG_PLATFORM, platform);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            apiUrl = getArguments().getString(ARG_API_URL);
            platform = (Platform) getArguments().getSerializable(ARG_PLATFORM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_social_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.social_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        searchBar = view.findViewById(R.id.search_bar);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);

        setupRecyclerView();
        setupHeaderInteractions(view);
        setupSwipeRefresh();
        setupSearchBar();
        setupChipGroupFilters();
        loadFeed();
    }

    private void setupRecyclerView() {
        adapter = new SocialPostAdapter(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(this::loadFeed);
        }
    }

    private void setupSearchBar() {
        if (searchBar != null) {
            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearch = s.toString();
                    filterAndShowPosts();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private TextView txtSocialTitle;
    private ImageView btnLikedPosts;
    private ImageView btnBookmarkPosts;
    private ImageView btnViewdPosts;
    private View btnBack;

    private void setupHeaderInteractions(View view) {
        txtSocialTitle = view.findViewById(R.id.txt_social_title);
        btnLikedPosts = view.findViewById(R.id.liked_posts);
        btnBookmarkPosts = view.findViewById(R.id.bookmark_posts);
        btnViewdPosts = view.findViewById(R.id.viewd_posts);
        btnBack = view.findViewById(R.id.bt_back);

        if (btnLikedPosts != null) {
            btnLikedPosts.setOnClickListener(v -> {
                showOnlyLiked = true;
                showOnlySaved = false;
                selectedPlatforms.clear(); // Limpa filtros de plataforma, ou mantém se quiser filtrar dentro dos
                                           // curtidos

                updateHeaderState("Curtidos");
                filterAndShowPosts();
            });
        }

        if (btnBookmarkPosts != null) {
            btnBookmarkPosts.setOnClickListener(v -> {
                showOnlyLiked = false;
                showOnlySaved = true;
                selectedPlatforms.clear();

                updateHeaderState("Salvos");
                filterAndShowPosts();
            });
        }

        if (btnViewdPosts != null) {
            btnViewdPosts.setOnClickListener(v -> {
                if (adapter != null) {
                    adapter.clearHiddenPosts();
                }
            });
        }

        if (adapter != null) {
            adapter.setOnHiddenPostsChangedListener(count -> {
                if (btnViewdPosts == null)
                    return;

                if (count > 0) {
                    btnViewdPosts.setImageResource(R.drawable.view_post_actived);
                } else {
                    btnViewdPosts.setImageResource(R.drawable.view_post_not_actived);
                }
            });
        }

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Se estiver mostrando curtidos ou salvos, volta para "Todos"
                if (showOnlyLiked || showOnlySaved) {
                    showOnlyLiked = false;
                    showOnlySaved = false;
                    selectedPlatforms.clear(); // Limpa filtros ao voltar

                    resetHeaderState();
                    updateChipsVisualState();
                    filterAndShowPosts();
                } else {
                    // Senão, volta a tela anterior (comportamento padrão)
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
        }
    }

    private void updateHeaderState(String title) {
        if (txtSocialTitle != null) {
            txtSocialTitle.setText(title);
        }

        // Oculta os ícones quando entra em uma sub-view
        if (btnLikedPosts != null)
            btnLikedPosts.setVisibility(View.GONE);
        if (btnBookmarkPosts != null)
            btnBookmarkPosts.setVisibility(View.GONE);
        if (btnViewdPosts != null)
            btnViewdPosts.setVisibility(View.GONE);

        // Aqui poderia mostrar um botão de voltar extra se necessário,
        // mas o design pede apenas para mudar o título e ocultar ícones.
        // A navegação de volta para "Redes Sociais" (Todos) precisa ser definida.
        // Assumindo que o botão de voltar padrão (bt_back) ou clicar em "Todos"
        // restaura.

        updateChipsVisualState();
    }

    // Método para restaurar o estado inicial (chamado ao clicar em "Todos" ou
    // voltar)
    private void resetHeaderState() {
        if (txtSocialTitle != null) {
            txtSocialTitle.setText("Redes Sociais");
        }

        if (btnLikedPosts != null)
            btnLikedPosts.setVisibility(View.VISIBLE);
        if (btnBookmarkPosts != null)
            btnBookmarkPosts.setVisibility(View.VISIBLE);
        if (btnViewdPosts != null)
            btnViewdPosts.setVisibility(View.VISIBLE);
    }

    private void setupChipGroupFilters() {
        if (chipGroupFilters == null)
            return;

        chipGroupFilters.removeAllViews();
        platformChips.clear();

        // Cria um contexto com tema MaterialComponents para os Chips
        Context themedContext = new ContextThemeWrapper(
                requireContext(),
                R.style.Theme_MaterialComponents);

        // Chip "Todos"
        chipTodos = (Chip) LayoutInflater.from(themedContext).inflate(R.layout.item_filter_chip, chipGroupFilters,
                false);
        chipTodos.setText("Todos");
        chipTodos.setCheckable(true);
        chipTodos.setCheckedIconVisible(true);
        chipTodos.setChecked(platform == null && selectedPlatforms.isEmpty());
        chipTodos.setOnClickListener(v -> {
            if (chipTodos.isChecked()) {
                // Se "Todos" foi selecionado, limpa todas as seleções
                selectedPlatforms.clear();
                showOnlyLiked = false;
                showOnlySaved = false;
                resetHeaderState(); // Restaura o cabeçalho
                updateChipsVisualState();
            }
            filterAndShowPosts();
        });
        chipGroupFilters.addView(chipTodos);

        // Filtros específicos para cada plataforma: Facebook, Instagram, X e YouTube
        Platform[] platformsToShow = { Platform.facebook, Platform.instagram, Platform.x, Platform.youtube };

        for (Platform p : platformsToShow) {
            Chip chip = (Chip) LayoutInflater.from(themedContext).inflate(R.layout.item_filter_chip,
                    chipGroupFilters, false);
            chip.setText(p.getDisplayName());
            chip.setCheckable(true);
            chip.setChecked(p == platform || selectedPlatforms.contains(p));

            // Configurar ícone da plataforma
            chip.setChipIconVisible(true);
            int iconRes = 0;
            if (p == Platform.facebook)
                iconRes = R.drawable.ic_facebook;
            else if (p == Platform.instagram)
                iconRes = R.drawable.ic_instagram;
            else if (p == Platform.youtube)
                iconRes = R.drawable.ic_youtube;
            else if (p == Platform.x)
                iconRes = R.drawable.ic_x;

            if (iconRes != 0) {
                chip.setChipIconResource(iconRes);
            }
            // Remove o ícone de check padrão para as redes sociais
            chip.setCheckedIconVisible(false);

            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    selectedPlatforms.add(p);
                    // Se uma plataforma foi selecionada, desmarca "Todos"
                    if (chipTodos != null && chipTodos.isChecked()) {
                        chipTodos.setChecked(false);
                    }
                } else {
                    selectedPlatforms.remove(p);
                    // Se nenhuma plataforma está selecionada, marca "Todos"
                    if (selectedPlatforms.isEmpty() && chipTodos != null) {
                        chipTodos.setChecked(true);
                    }
                }
                filterAndShowPosts();
            });
            chipGroupFilters.addView(chip);
            platformChips.add(chip);
        }

        // Se veio com uma plataforma inicial, adiciona às selecionadas
        if (platform != null) {
            selectedPlatforms.add(platform);
            updateChipsVisualState();
        }
    }

    /**
     * Atualiza o estado visual dos chips baseado nas seleções atuais
     */
    private void updateChipsVisualState() {
        if (chipTodos != null) {
            // "Todos" deve estar marcado se nenhuma plataforma específica estiver
            // selecionada,
            // independentemente de estar mostrando apenas curtidos ou salvos.
            chipTodos.setChecked(selectedPlatforms.isEmpty());
        }
        if (chipLiked != null) {
            chipLiked.setChecked(showOnlyLiked);
        }
        if (chipSaved != null) {
            chipSaved.setChecked(showOnlySaved);
        }
        for (Chip chip : platformChips) {
            // Encontra qual plataforma este chip representa
            Platform chipPlatform = findPlatformForChip(chip);
            if (chipPlatform != null) {
                chip.setChecked(selectedPlatforms.contains(chipPlatform));
            }
        }
    }

    /**
     * Encontra a plataforma correspondente a um chip
     */
    private Platform findPlatformForChip(Chip chip) {
        String chipText = chip.getText().toString();
        for (Platform p : Platform.values()) {
            if (p.getDisplayName().equals(chipText)) {
                return p;
            }
        }
        return null;
    }

    private void loadFeed() {
        showLoading(true);

        SocialFeedService.fetchFeed(apiUrl, new SocialFeedService.SocialFeedCallback() {
            @Override
            public void onSuccess(SocialFeedResponse response) {
                if (isAdded() && getActivity() != null) {
                    showLoading(false);

                    if (response.getItems().isEmpty()) {
                        showEmpty(true);
                        allPosts.clear();
                    } else {
                        allPosts.clear();
                        allPosts.addAll(response.getItems());

                        // Se veio com uma plataforma inicial, adiciona às selecionadas
                        if (platform != null && !selectedPlatforms.contains(platform)) {
                            selectedPlatforms.add(platform);
                            updateChipsVisualState();
                        }

                        filterAndShowPosts();
                    }
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded() && getActivity() != null) {
                    showLoading(false);
                    showEmpty(true);
                    allPosts.clear();
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Verifica se a plataforma do post corresponde ao filtro selecionado
     */
    private boolean matchesPlatformFilter(String postPlatform, Platform filterPlatform) {
        if (postPlatform == null || filterPlatform == null) {
            return false;
        }

        String normalizedPost = postPlatform.toLowerCase().trim();
        String normalizedFilter = filterPlatform.name().toLowerCase();

        // Caso especial: X também aceita "twitter"
        if (filterPlatform == Platform.x) {
            return normalizedPost.equals("x") || normalizedPost.equals("twitter");
        }

        return normalizedPost.equals(normalizedFilter);
    }

    /**
     * Verifica se o post corresponde a alguma das plataformas selecionadas
     */
    private boolean matchesAnySelectedPlatform(SocialPost post) {
        // Se nenhuma plataforma está selecionada (ou "Todos" está selecionado), aceita
        // todas
        if (selectedPlatforms.isEmpty()) {
            return true;
        }

        // Verifica se o post pertence a alguma das plataformas selecionadas
        for (Platform selectedPlatform : selectedPlatforms) {
            if (post.getPlatform() != null &&
                    matchesPlatformFilter(post.getPlatform(), selectedPlatform)) {
                return true;
            }
        }

        return false;
    }

    private void filterAndShowPosts() {
        List<SocialPost> filtered = new ArrayList<>();

        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME,
                android.content.Context.MODE_PRIVATE);

        for (SocialPost post : allPosts) {
            if (showOnlyLiked) {
                boolean isLiked = prefs.getBoolean(KEY_LIKES + post.getId(), false);
                if (!isLiked)
                    continue;
            }

            if (showOnlySaved) {
                boolean isSaved = prefs.getBoolean(KEY_BOOKMARKS + post.getId(), false);
                if (!isSaved)
                    continue;
            }

            // Filtro de plataforma (permite múltiplas)
            boolean matchesPlatform = matchesAnySelectedPlatform(post);

            boolean matchesSearch = currentSearch.isEmpty() ||
                    (post.getText() != null && post.getText().toLowerCase().contains(currentSearch.toLowerCase()));

            if (matchesPlatform && matchesSearch) {
                filtered.add(post);
            }
        }

        // Atualiza mensagem de vazio
        if (filtered.isEmpty()) {
            if (showOnlyLiked) {
                emptyView.setText("Nenhum post curtido ainda ❤️");
            } else if (showOnlySaved) {
                emptyView.setText("Nenhum post salvo ainda 🔖");
            } else if (!selectedPlatforms.isEmpty()) {
                emptyView.setText("Nenhum post encontrado para as redes selecionadas");
            } else {
                emptyView.setText("Nenhum post encontrado");
            }
            showEmpty(true);
        } else {
            showEmpty(false);
            adapter.setPosts(filtered);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (swipeRefresh != null && !show) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void showEmpty(boolean show) {
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pausa o vídeo que está tocando quando o fragment é pausado
        if (adapter != null) {
            adapter.pauseCurrentVideo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Os vídeos não retomam automaticamente ao voltar ao fragment
        // O usuário precisa clicar novamente para reproduzir
    }

    /**
     * Pausa a WebView de vídeo de background da MainActivity
     */

}
