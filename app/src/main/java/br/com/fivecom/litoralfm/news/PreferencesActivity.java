package br.com.fivecom.litoralfm.news;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.news.Categoria;
import br.com.fivecom.litoralfm.news.CategoriasCallback;
import br.com.fivecom.litoralfm.news.PreferencesPagerAdapter;
import br.com.fivecom.litoralfm.R;

@SuppressWarnings({"unused", "WeakerAccess"})
public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";
    private static final String KEY_SELECTED = "prefs_selected_list";
    private static final String PREFS_NAME = "app_prefs";
    private static final String PREFS_KEY_SELECTED = "prefs_selected";
    private static final String PREFS_KEY_FIRST_TIME = "prefs_first_time";

    private ViewPager2 viewPager;
    private NoticiaService noticiaService;
    private List<Categoria> categoriasList = new ArrayList<>();

    // Referências aos fragments para preenchimento de categorias
    private PagerOneFragment pagerOneFragment;
    private PagerSecondFragment pagerSecondFragment;
    private PagerThirdFragment pagerThirdFragment;

    // Keep selected categories in FIFO order
    private final Deque<String> selectedQueue = new LinkedList<>();

    // Map categoryId -> ImageView (check) weak references
    private final Map<String, WeakReference<ImageView>> checkViews = new HashMap<>();

    // Keep references to all btn_ready views (there's one per page layout)
    private final LinkedList<WeakReference<View>> readyButtons = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar se é primeira vez (opcional - pode ser usado por outras activities)
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isFirstTime = sp.getBoolean(PREFS_KEY_FIRST_TIME, true);
        if (isFirstTime) {
            sp.edit().putBoolean(PREFS_KEY_FIRST_TIME, false).apply();
        }

        // Restore selected queue if available
        if (savedInstanceState != null) {
            ArrayList<String> list = savedInstanceState.getStringArrayList(KEY_SELECTED);
            if (list != null) {
                selectedQueue.addAll(list);
            }
        }

        setContentView(R.layout.activity_preferences_pager);

        viewPager = findViewById(R.id.viewPager);
        PreferencesPagerAdapter adapter = new PreferencesPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0, false);

        // Inicializar serviço de notícias
        noticiaService = NoticiaService.getInstance();

        // Buscar categorias da API e preencher views
        loadCategoriasAndPopulateViews();

        // Restore previously saved selection from SharedPreferences
        try {
            java.util.Set<String> saved = sp.getStringSet(PREFS_KEY_SELECTED, null);
            if (saved != null && selectedQueue.isEmpty()) {
                selectedQueue.addAll(saved);
            }
        } catch (Exception ignored) {
        }
    }

    private void loadCategoriasAndPopulateViews() {
        noticiaService.getCategorias(new CategoriasCallback() {
            @Override
            public void onSuccess(List<Categoria> categorias) {
                categoriasList = categorias;
                Log.d(TAG, "Categorias carregadas: " + categorias.size());

                // Preencher TextViews das categorias nas views
                runOnUiThread(() -> populateCategoryTextViews(categorias));
            }
        });
    }

    private void populateCategoryTextViews(List<Categoria> categorias) {
        // Preencher TextViews nos fragments
        String[] pageOneNames = new String[6];
        String[] pageTwoNames = new String[6];
        String[] pageThreeNames = new String[6];

        for (int i = 0; i < categorias.size() && i < 18; i++) {
            Categoria cat = categorias.get(i);
            if (i < 6) {
                pageOneNames[i] = cat.getNome();
            } else if (i < 12) {
                pageTwoNames[i - 6] = cat.getNome();
            } else {
                pageThreeNames[i - 12] = cat.getNome();
            }
        }

        // Preencher primeira página
        if (pagerOneFragment != null) {
            pagerOneFragment.populateCategoryTexts(pageOneNames);
        }

        // Preencher segunda página
        if (pagerSecondFragment != null) {
            pagerSecondFragment.populateCategoryTexts(pageTwoNames);
        }

        // Preencher terceira página
        if (pagerThirdFragment != null) {
            pagerThirdFragment.populateCategoryTexts(pageThreeNames);
        }
    }

    // Método chamado pelos fragments para se registrar
    public void registerFragmentForCategoryPopulation(Fragment fragment) {
        if (fragment instanceof PagerOneFragment) {
            pagerOneFragment = (PagerOneFragment) fragment;
            // Se as categorias já foram carregadas, preencher agora
            if (!categoriasList.isEmpty()) {
                populateCategoryTextViews(categoriasList);
            }
        } else if (fragment instanceof PagerSecondFragment) {
            pagerSecondFragment = (PagerSecondFragment) fragment;
            if (!categoriasList.isEmpty()) {
                populateCategoryTextViews(categoriasList);
            }
        } else if (fragment instanceof PagerThirdFragment) {
            pagerThirdFragment = (PagerThirdFragment) fragment;
            if (!categoriasList.isEmpty()) {
                populateCategoryTextViews(categoriasList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        if (outState == null) {
            outState = new Bundle();
        }
        ArrayList<String> list = new ArrayList<>(selectedQueue);
        outState.putStringArrayList(KEY_SELECTED, list);
        super.onSaveInstanceState(outState);
    }

    // Universal onClick handler referenced from XML via android:onClick
    public void onClick(View v) {
        int id = v.getId();
        
        // Primeira página
        if (id == R.id.vl_one) {
            toggleCategory("vl_one");
            return;
        } else if (id == R.id.vl_two) {
            toggleCategory("vl_two");
            return;
        } else if (id == R.id.vl_three) {
            toggleCategory("vl_three");
            return;
        } else if (id == R.id.vl_four) {
            toggleCategory("vl_four");
            return;
        } else if (id == R.id.vl_five) {
            toggleCategory("vl_five");
            return;
        } else if (id == R.id.vl_six) {
            toggleCategory("vl_six");
            return;
        }

        // Segunda página
        if (id == R.id.vl_seven) {
            toggleCategory("vl_seven");
            return;
        } else if (id == R.id.vl_eigth) {
            toggleCategory("vl_eigth");
            return;
        } else if (id == R.id.vl_nine) {
            toggleCategory("vl_nine");
            return;
        } else if (id == R.id.vl_ten) {
            toggleCategory("vl_ten");
            return;
        } else if (id == R.id.vl_eleven) {
            toggleCategory("vl_eleven");
            return;
        } else if (id == R.id.vl_twelve) {
            toggleCategory("vl_twelve");
            return;
        }

        // Terceira página
        if (id == R.id.vl_thirteen) {
            toggleCategory("vl_thirteen");
            return;
        } else if (id == R.id.vl_fourteen) {
            toggleCategory("vl_fourteen");
            return;
        } else if (id == R.id.vl_fifiteen) {
            toggleCategory("vl_fifiteen");
            return;
        } else if (id == R.id.vl_sixteen) {
            toggleCategory("vl_sixteen");
            return;
        } else if (id == R.id.vl_seventeen) {
            toggleCategory("vl_seventeen");
            return;
        } else if (id == R.id.vl_eigteen) {
            toggleCategory("vl_eigteen");
            return;
        }

        // Navegação entre páginas
        if (id == R.id.btn_preferences_one) {
            int pos = viewPager != null ? viewPager.getCurrentItem() : 0;
            if (pos > 0) goToPage(pos - 1);
            return;
        } else if (id == R.id.btn_preferences_second) {
            int pos = viewPager != null ? viewPager.getCurrentItem() : 0;
            if (pos > 0) goToPage(pos - 1);
            return;
        } else if (id == R.id.btn_preferences_third) {
            int pos = viewPager != null ? viewPager.getCurrentItem() : 0;
            if (viewPager != null && pos < viewPager.getAdapter().getItemCount() - 1) {
                goToPage(pos + 1);
            }
            return;
        } else if (id == R.id.des) {
            int pos = viewPager != null ? viewPager.getCurrentItem() : 0;
            if (viewPager != null && pos < viewPager.getAdapter().getItemCount() - 1) {
                goToPage(pos + 1);
            }
            return;
        }

        // Botão Pronto
        if (id == R.id.btn_ready) {
            if (selectedQueue.size() == 3) {
                // Persistir seleção em SharedPreferences
                SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                HashSet<String> set = new HashSet<>(selectedQueue);
                sp.edit().putStringSet(PREFS_KEY_SELECTED, set).apply();

                // Preparar intent de resultado
                Intent result = new Intent();
                result.putStringArrayListExtra("selected", new ArrayList<>(selectedQueue));

                // Se foi iniciado para resultado, retornar para o chamador
                if (getCallingActivity() != null) {
                    setResult(RESULT_OK, result);
                } else {
                   return;
                }

                Toast.makeText(this, "Preferências salvas", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Escolha exatamente 3 categorias", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    // Navigation helper
    public void goToPage(int index) {
        if (viewPager != null) {
            viewPager.setCurrentItem(index, true);
        }
    }

    // Registration helpers called from fragments so Activity can manage state/UI
    public void registerCategoryCheckView(String categoryId, ImageView checkView) {
        if (categoryId == null || checkView == null) return;
        checkViews.put(categoryId, new WeakReference<>(checkView));

        boolean isSelected = selectedQueue.contains(categoryId);
        checkView.setVisibility(isSelected ? View.VISIBLE : View.GONE);
    }

    public void registerReadyButton(View btnReady) {
        if (btnReady == null) return;
        readyButtons.add(new WeakReference<>(btnReady));
        btnReady.setVisibility(selectedQueue.size() == 3 ? View.VISIBLE : View.GONE);
    }

    // Toggle selection for a category id (called by fragments or onClick XML)
    public synchronized void toggleCategory(String categoryId) {
        if (categoryId == null) return;

        if (selectedQueue.contains(categoryId)) {
            // Deselecionar
            selectedQueue.remove(categoryId);
            setCheckVisibility(categoryId, false);
        } else {
            // Selecionar - se já tiver 3, remover a primeira (FIFO)
            if (selectedQueue.size() >= 3) {
                String removed = selectedQueue.pollFirst();
                if (removed != null) {
                    setCheckVisibility(removed, false);
                }
            }
            selectedQueue.addLast(categoryId);
            setCheckVisibility(categoryId, true);
        }

        updateReadyButtons();

        // Persistir seleção atual
        persistSelectedSet();
    }

    // Persistir categorias selecionadas para SharedPreferences
    private void persistSelectedSet() {
        try {
            SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            java.util.Set<String> set = new HashSet<>(selectedQueue);
            sp.edit().putStringSet(PREFS_KEY_SELECTED, set).apply();
        } catch (Exception ignored) {
        }
    }

    private void setCheckVisibility(String categoryId, boolean visible) {
        WeakReference<ImageView> ref = checkViews.get(categoryId);
        ImageView iv = ref != null ? ref.get() : null;
        if (iv != null) {
            iv.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void updateReadyButtons() {
        boolean show = selectedQueue.size() == 3;
        for (WeakReference<View> ref : readyButtons) {
            View t = ref.get();
            if (t != null) {
                t.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    // Utility: verificar se uma categoria está selecionada
    public boolean isCategorySelected(String categoryId) {
        return selectedQueue.contains(categoryId);
    }
}

