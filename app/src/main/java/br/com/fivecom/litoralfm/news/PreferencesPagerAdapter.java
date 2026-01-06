package br.com.fivecom.litoralfm.news;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PreferencesPagerAdapter extends FragmentStateAdapter {

    public PreferencesPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return PagerOneFragment.newInstance();
            case 1:
                return PagerSecondFragment.newInstance();
            case 2:
            default:
                return PagerThirdFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}







