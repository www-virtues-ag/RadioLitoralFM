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

public class PagerOneFragment extends Fragment {

    private View rootView;

    public static PagerOneFragment newInstance() {
        return new PagerOneFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_pager_one, container, false);

        final PreferencesActivity prefs = (getActivity() instanceof PreferencesActivity)
                ? (PreferencesActivity) getActivity()
                : null;

        if (prefs != null) {
            // Registrar views de check
            registerCategory(rootView, prefs, R.id.vl_one, R.id.check_c1, "vl_one");
            registerCategory(rootView, prefs, R.id.vl_two, R.id.check_c2, "vl_two");
            registerCategory(rootView, prefs, R.id.vl_three, R.id.check_c3, "vl_three");
            registerCategory(rootView, prefs, R.id.vl_four, R.id.check_c4, "vl_four");
            registerCategory(rootView, prefs, R.id.vl_five, R.id.check_c5, "vl_five");
            registerCategory(rootView, prefs, R.id.vl_six, R.id.check_c6, "vl_six");

            // Registrar botão Pronto
            View btnReady = rootView.findViewById(R.id.btn_ready);
            if (btnReady != null) {
                prefs.registerReadyButton(btnReady);
            }

            // Configurar setas - primeira página
            ImageView left = rootView.findViewById(R.id.btn_preferences_one);
            if (left != null) {
                left.setEnabled(false);
                left.setAlpha(0.75f);
            }

            ImageView right = rootView.findViewById(R.id.btn_preferences_third);
            if (right != null) {
                right.setEnabled(true);
                right.setAlpha(1.0f);
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
        
        populateTextView(rootView, R.id.vl_one, categoryNames.length > 0 ? categoryNames[0] : null);
        populateTextView(rootView, R.id.vl_two, categoryNames.length > 1 ? categoryNames[1] : null);
        populateTextView(rootView, R.id.vl_three, categoryNames.length > 2 ? categoryNames[2] : null);
        populateTextView(rootView, R.id.vl_four, categoryNames.length > 3 ? categoryNames[3] : null);
        populateTextView(rootView, R.id.vl_five, categoryNames.length > 4 ? categoryNames[4] : null);
        populateTextView(rootView, R.id.vl_six, categoryNames.length > 5 ? categoryNames[5] : null);
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

