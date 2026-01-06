package br.com.fivecom.litoralfm.news;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.databinding.FragmentNewBinding;
import br.com.fivecom.litoralfm.models.news.Categoria;
import br.com.fivecom.litoralfm.models.news.Noticia;
import br.com.fivecom.litoralfm.ui.main.MainActivity;
import br.com.fivecom.litoralfm.ui.news.CategoryAdapter;
import br.com.fivecom.litoralfm.ui.news.NewsAdapter;
import br.com.fivecom.litoralfm.ui.news.network.CategoriasCallback;
import br.com.fivecom.litoralfm.ui.news.network.NoticiasCallback;
import br.com.fivecom.litoralfm.ui.news.network.NoticiaService;
import br.com.fivecom.litoralfm.utils.constants.Constants;
import br.com.fivecom.litoralfm.utils.core.Intents;

import static br.com.fivecom.litoralfm.utils.constants.Constants.data;

@UnstableApi
public class NewsFragment extends Fragment implements View.OnClickListener, CategoryAdapter.OnCategorySelectionListener {

    private FragmentNewBinding binding;
    private FrameLayout contentLayout;
    private MainActivity activity;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private NewsAdapter newsAdapter;

    // Data
    private List<Categoria> allCategorias = new ArrayList<>();

    // Filtros
    private Set<Integer> selectedCategoryIds = new HashSet<>();
    private String searchQuery = "";

    // Handler para debounce da busca
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Paginação
    private static final int ITEMS_PER_PAGE = 10; // Aumentado para 10 notícias por página
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMoreItems = true;
    private String currentCacheKey = "";
    private long lastLoadTime = 0;
    private static final long MIN_LOAD_INTERVAL_MS = 500; // 500ms entre carregamentos

    // WebView do banner
    private WebView webView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        contentLayout = new FrameLayout(activity);
        binding = FragmentNewBinding.inflate(inflater, contentLayout, false);
        contentLayout.addView(binding.getRoot());
        return contentLayout;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Components();
    }

    private void Components() {
        setupClickListeners();
        setupCategoryRecyclerView();
        setupNewsRecyclerView();
        setupSearchField();
        setupWebView();
        loadCategorias();
        // Notícias serão carregadas quando uma categoria for selecionada
    }

    private void setupClickListeners() {
        if (binding.btBack != null) {
            binding.btBack.setOnClickListener(this);
        }
        if (binding.btHome != null) {
            binding.btHome.setOnClickListener(this);
        }
        if (binding.btMenu != null) {
            binding.btMenu.setOnClickListener(this);
        }
        if (binding.btNotif != null) {
            binding.btNotif.setOnClickListener(this);
        }
    }

    private void setupCategoryRecyclerView() {
        if (binding.rvCategory == null) return;

        // GridLayoutManager com 3 colunas
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 3);
        binding.rvCategory.setLayoutManager(layoutManager);

        // Adapter
        categoryAdapter = new CategoryAdapter(requireContext(), allCategorias);
        categoryAdapter.setOnCategorySelectionListener(this);
        binding.rvCategory.setAdapter(categoryAdapter);
    }

    private void setupNewsRecyclerView() {
        if (binding.rvNoticias == null) return;

        // LinearLayoutManager vertical
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.rvNoticias.setLayoutManager(layoutManager);

        // Adapter
        newsAdapter = new NewsAdapter(requireContext());
        binding.rvNoticias.setAdapter(newsAdapter);

        // Scroll listener para paginação
        binding.rvNoticias.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                // Só processa se estiver rolando para baixo
                if (dy <= 0) return;
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager == null) return;
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                
                // Carrega mais quando está próximo do final (últimos 10 itens)
                // Com debounce para evitar múltiplas chamadas rápidas
                if (!isLoading && hasMoreItems && totalItemCount > 0) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastLoadTime > MIN_LOAD_INTERVAL_MS) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 10) {
                            lastLoadTime = currentTime;
                            loadNextPage();
                        }
                    }
                }
            }
        });
    }

    private void setupSearchField() {
        EditText searchEditText = binding.formaCampoNoticia;
        if (searchEditText == null) return;

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Remove callbacks anteriores
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Novo callback com debounce de 500ms
                    searchRunnable = () -> {
                        searchQuery = s.toString().trim();
                        // Aplica filtro de busca nas notícias já carregadas
                        if (newsAdapter != null) {
                            // Recarrega todas as páginas com filtro
                            resetPagination();
                            loadNoticiasByCategory();
                        }
                    };
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Configura a WebView do banner
     */
    private void setupWebView() {
        if (binding == null || binding.webView == null) return;

        webView = binding.webView;
        webView.setBackgroundColor(0x00000000);
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (getContext() != null && isAdded()) {
                    Intents.website_internal(getContext(), url);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (view != null && isAdded()) {
                    view.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (view != null && isAdded()) {
                    view.setVisibility(View.GONE);
                }
            }
        });

        // Carrega a URL do banner
        if (data != null && data.radios != null && !data.radios.isEmpty() && Constants.ID >= 0 && Constants.ID < data.radios.size()) {
            String pubUrl = String.format(
                    Intents.decode(getString(R.string.pub)),
                    data.radios.get(Constants.ID).id,
                    "Android " + Build.VERSION.RELEASE,
                    Build.MANUFACTURER + " - " + Build.MODEL
            );
            webView.loadUrl(pubUrl);
            Log.d("NewsFragment", "✅ WebView carregando URL: " + pubUrl);
        } else {
            Log.w("NewsFragment", "⚠️ Dados da rádio não disponíveis para carregar o banner");
            webView.setVisibility(View.GONE);
        }
    }

    private void loadCategorias() {
        NoticiaService.getInstance().getCategorias(new CategoriasCallback() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                if (isAdded() && categoryAdapter != null) {
                    allCategorias.clear();
                    allCategorias.addAll(categorias);
                    categoryAdapter.setCategorias(allCategorias);
                    
                    // Seleciona "Todas" por padrão (ID 0)
                    if (!allCategorias.isEmpty() && allCategorias.get(0).getId() == 0) {
                        Set<Integer> defaultSelection = new HashSet<>();
                        defaultSelection.add(0);
                        selectedCategoryIds = defaultSelection;
                        categoryAdapter.setSelectedCategoryIds(defaultSelection);
                        loadNoticiasByCategory();
                    }
                    
                    Log.d("NewsFragment", "✅ Categorias carregadas: " + categorias.size());
                }
            }
        });
    }

    @Override
    public void onCategorySelectionChanged(Set<Integer> selectedIds) {
        selectedCategoryIds = new HashSet<>(selectedIds);
        // Reseta paginação e carrega primeira página
        resetPagination();
        loadNoticiasByCategory();
    }

    private void resetPagination() {
        currentPage = 1;
        isLoading = false;
        hasMoreItems = true;
        if (newsAdapter != null) {
            newsAdapter.setNoticias(new ArrayList<>());
        }
    }

    private void loadNoticiasByCategory() {
        loadNoticiasByCategory(1, false);
    }

    private void loadNextPage() {
        if (isLoading || !hasMoreItems) {
            return; // Já está carregando ou não há mais itens
        }
        loadNoticiasByCategory(currentPage + 1, true);
    }

    private void loadNoticiasByCategory(int page, boolean append) {
        if (isLoading) {
            Log.d("NewsFragment", "⏸️ Já está carregando, ignorando requisição");
            return;
        }
        
        isLoading = true;
        currentPage = page;
        
        // Se não há nenhuma categoria selecionada, seleciona "Todas" como padrão
        if (selectedCategoryIds.isEmpty()) {
            selectedCategoryIds.add(0);
            // Atualiza o adapter de categorias
            if (categoryAdapter != null) {
                categoryAdapter.setSelectedCategoryIds(selectedCategoryIds);
            }
        }
        
        // Se "Todas" está selecionada (ID 0), busca sem filtro
        if (selectedCategoryIds.contains(0)) {
            loadNoticiasForSlug(null, page, append);
            return;
        }
        
        // Coleta todos os slugs das categorias selecionadas
        List<String> slugs = new ArrayList<>();
        for (Integer categoryId : selectedCategoryIds) {
            for (Categoria cat : allCategorias) {
                if (cat.getId() == categoryId && !cat.getSlug().equals("todas")) {
                    slugs.add(cat.getSlug());
                    break;
                }
            }
        }
        
        if (slugs.isEmpty()) {
            // Se não encontrou slugs, usa "Todas" como padrão
            selectedCategoryIds.clear();
            selectedCategoryIds.add(0);
            if (categoryAdapter != null) {
                categoryAdapter.setSelectedCategoryIds(selectedCategoryIds);
            }
            loadNoticiasForSlug(null, page, append);
            return;
        }
        
        // Se há múltiplas categorias, mescla os resultados
        if (slugs.size() > 1) {
            loadNoticiasMultipleCategories(slugs, page, append);
        } else {
            // Apenas uma categoria, usa o método normal
            loadNoticiasForSlug(slugs.get(0), page, append);
        }
    }

    private void loadNoticiasMultipleCategories(List<String> slugs, int page, boolean append) {
        final int totalRequests = slugs.size();
        final List<List<Noticia>> allResults = new ArrayList<>();
        final int[] completedRequests = {0};
        final boolean[] hasError = {false};
        
        Log.d("NewsFragment", "🔄 Carregando " + totalRequests + " categorias: " + slugs);
        
        // Faz requisições paralelas para cada slug
        for (String slug : slugs) {
            NoticiaService.getInstance().fetchNoticiasPaginated(slug, page, ITEMS_PER_PAGE, new NoticiasCallback() {
                @Override
                public void onSuccess(List<Noticia> noticias) {
                    synchronized (allResults) {
                        allResults.add(noticias);
                        completedRequests[0]++;
                        
                        // Quando todas as requisições terminarem
                        if (completedRequests[0] == totalRequests && isAdded()) {
                            mergeAndDisplayResults(allResults, append);
                        }
                    }
                }
                
                @Override
                public void onError(String error) {
                    synchronized (allResults) {
                        completedRequests[0]++;
                        hasError[0] = true;
                        Log.e("NewsFragment", "❌ Erro ao carregar categoria: " + error);
                        
                        // Quando todas as requisições terminarem (mesmo com erro)
                        if (completedRequests[0] == totalRequests && isAdded()) {
                            if (hasError[0] && allResults.isEmpty()) {
                                isLoading = false;
                                hasMoreItems = false;
                                if (!append) {
                                    Toast.makeText(requireContext(), "Erro ao carregar notícias", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mergeAndDisplayResults(allResults, append);
                            }
                        }
                    }
                }
            });
        }
    }

    private void mergeAndDisplayResults(List<List<Noticia>> allResults, boolean append) {
        // Mescla todas as listas
        List<Noticia> merged = new ArrayList<>();
        for (List<Noticia> list : allResults) {
            merged.addAll(list);
        }
        
        // Remove duplicatas baseado no ID
        Set<Integer> seenIds = new HashSet<>();
        List<Noticia> uniqueNoticias = new ArrayList<>();
        for (Noticia noticia : merged) {
            if (!seenIds.contains(noticia.getId())) {
                seenIds.add(noticia.getId());
                uniqueNoticias.add(noticia);
            }
        }
        
        // Ordena por data (mais recentes primeiro)
        Collections.sort(uniqueNoticias, (n1, n2) -> {
            String data1 = n1.getData() != null ? n1.getData() : "";
            String data2 = n2.getData() != null ? n2.getData() : "";
            return data2.compareTo(data1); // Ordem decrescente
        });
        
        // Aplica paginação no resultado mesclado
        int offset = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(offset + ITEMS_PER_PAGE, uniqueNoticias.size());
        
        List<Noticia> pageNoticias;
        if (offset >= uniqueNoticias.size()) {
            pageNoticias = new ArrayList<>();
            hasMoreItems = false;
        } else {
            pageNoticias = new ArrayList<>(uniqueNoticias.subList(offset, endIndex));
            // Verifica se há mais itens
            hasMoreItems = endIndex < uniqueNoticias.size();
        }
        
        // Aplica filtro de busca se houver
        List<Noticia> filtered = applySearchFilterToList(pageNoticias);
        
        isLoading = false;
        
        if (newsAdapter != null) {
            if (append) {
                newsAdapter.addNoticias(filtered);
            } else {
                newsAdapter.setNoticias(filtered);
            }
        }
        
        Log.d("NewsFragment", "✅ Mescladas " + uniqueNoticias.size() + " notícias, exibindo página " + currentPage + ": " + filtered.size() + " itens");
    }

    private void loadNoticiasForSlug(String sectionSlug, int page, boolean append) {
        NoticiaService.getInstance().fetchNoticiasPaginated(sectionSlug, page, ITEMS_PER_PAGE, new NoticiasCallback() {
            @Override
            public void onSuccess(List<Noticia> noticias) {
                if (isAdded() && newsAdapter != null) {
                    isLoading = false;
                    
                    if (noticias.isEmpty()) {
                        hasMoreItems = false;
                    } else {
                        // Verifica se recebeu menos itens que o esperado (última página)
                        if (noticias.size() < ITEMS_PER_PAGE) {
                            hasMoreItems = false;
                        }
                        
                        // Aplica filtro de busca se houver
                        List<Noticia> filtered = applySearchFilterToList(noticias);
                        
                        if (append) {
                            newsAdapter.addNoticias(filtered);
                        } else {
                            newsAdapter.setNoticias(filtered);
                        }
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                if (isAdded()) {
                    isLoading = false;
                    hasMoreItems = false;
                    Log.e("NewsFragment", "❌ Erro ao carregar notícias: " + error);
                    if (!append) {
                        Toast.makeText(requireContext(), "Erro ao carregar notícias", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private List<Noticia> applySearchFilterToList(List<Noticia> noticias) {
        if (searchQuery == null || searchQuery.isEmpty()) {
            return noticias;
        } else {
            List<Noticia> filtered = new ArrayList<>();
            for (Noticia noticia : noticias) {
                if (matchesSearch(noticia, searchQuery)) {
                    filtered.add(noticia);
                }
            }
            return filtered;
        }
    }

    private boolean matchesSearch(Noticia noticia, String query) {
        if (query == null || query.isEmpty()) return true;
        
        String lowerQuery = query.toLowerCase();
        String titulo = noticia.getTitulo() != null ? noticia.getTitulo().toLowerCase() : "";
        String descricao = noticia.getDescricao() != null ? noticia.getDescricao().toLowerCase() : "";
        String conteudo = noticia.getConteudo() != null ? noticia.getConteudo().toLowerCase() : "";

        return titulo.contains(lowerQuery) || 
               descricao.contains(lowerQuery) || 
               conteudo.contains(lowerQuery);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
                || newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (contentLayout != null) {
                contentLayout.removeAllViews();
                binding = FragmentNewBinding.inflate(getLayoutInflater(), contentLayout, false);
                contentLayout.addView(binding.getRoot());
                Components();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.bt_back) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
            }
        } else if (id == R.id.bt_home) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.MAIN);
            }
        } else if (id == R.id.bt_menu) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMenu();
            }
        } else if (id == R.id.bt_notif) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFragment(MainActivity.FRAGMENT.NOTF_PROGRAM);
            }
        }
    }
}

