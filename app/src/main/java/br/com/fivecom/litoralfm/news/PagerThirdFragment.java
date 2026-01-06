package br.com.fivecom.litoralfm.news;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import br.com.fivecom.litoralfm.news.PreferencesActivity;
import br.com.fivecom.litoralfm.R;

public class PagerThirdFragment extends Fragment {

    private View rootView;

    public static PagerThirdFragment newInstance() {
        return new PagerThirdFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pager_third, container, false);

        final PreferencesActivity prefs = (getActivity() instanceof PreferencesActivity)
                ? (PreferencesActivity) getActivity()
                : null;

        if (prefs != null) {
            // Registrar views de check
            registerCategory(rootView, prefs, R.id.vl_thirteen, R.id.check_c13, "vl_thirteen");
            registerCategory(rootView, prefs, R.id.vl_fourteen, R.id.check_c14, "vl_fourteen");
            registerCategory(rootView, prefs, R.id.vl_fifiteen, R.id.check_c15, "vl_fifiteen");
            registerCategory(rootView, prefs, R.id.vl_sixteen, R.id.check_c16, "vl_sixteen");
            registerCategory(rootView, prefs, R.id.vl_seventeen, R.id.check_c17, "vl_seventeen");
            registerCategory(rootView, prefs, R.id.vl_eigteen, R.id.check_c18, "vl_eigteen");

            // Registrar botão Pronto
            View btnReady = rootView.findViewById(R.id.btn_ready);
            if (btnReady != null) {
                prefs.registerReadyButton(btnReady);
            }

            // Configurar setas - terceira página
            ImageView left = rootView.findViewById(R.id.btn_preferences_second);
            if (left != null) {
                left.setEnabled(true);
                left.setAlpha(1.0f);
            }

            ImageView right = rootView.findViewById(R.id.des);
            if (right != null) {
                right.setEnabled(false);
                right.setAlpha(0.75f);
            }

            // Registrar fragment para preenchimento de categorias
            prefs.registerFragmentForCategoryPopulation(this);
        }

        return rootView;
    }

    private void registerCategory(View root, PreferencesActivity prefs, int categoryViewId, int checkViewId, String categoryKey) {
        ImageView check = root.findViewById(checkViewId);
        if (prefs != null && check != null) {
            prefs.registerCategoryCheckView(categoryKey, check);
        }
    }

    public void populateCategoryTexts(String[] categoryNames) {
        if (rootView == null || categoryNames == null) return;
        
        populateTextView(rootView, R.id.vl_thirteen, categoryNames.length > 0 ? categoryNames[0] : null);
        populateTextView(rootView, R.id.vl_fourteen, categoryNames.length > 1 ? categoryNames[1] : null);
        populateTextView(rootView, R.id.vl_fifiteen, categoryNames.length > 2 ? categoryNames[2] : null);
        populateTextView(rootView, R.id.vl_sixteen, categoryNames.length > 3 ? categoryNames[3] : null);
        populateTextView(rootView, R.id.vl_seventeen, categoryNames.length > 4 ? categoryNames[4] : null);
        populateTextView(rootView, R.id.vl_eigteen, categoryNames.length > 5 ? categoryNames[5] : null);
    }

    private void populateTextView(View root, int categoryViewId, String text) {
        if (text == null) return;
        View categoryView = root.findViewById(categoryViewId);
        if (categoryView instanceof ViewGroup) {
            TextView textView = findTextViewInView((ViewGroup) categoryView);
            if (textView != null) {
                textView.setText(text);
            }
        }
    }

    private TextView findTextViewInView(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof TextView) {
                return (TextView) child;
            } else if (child instanceof ViewGroup) {
                TextView found = findTextViewInView((ViewGroup) child);
                if (found != null) return found;
            }
        }
        return null;
    }
}
