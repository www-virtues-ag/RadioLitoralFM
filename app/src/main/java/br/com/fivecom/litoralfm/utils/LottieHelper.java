package br.com.fivecom.litoralfm.utils;

import android.content.Context;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.utils.constants.Preferences;

/**
 * Helper para gerenciar animações Lottie baseado no modo estático
 */
public class LottieHelper {

    /**
     * Aplica o estado de animação em um Lottie específico
     * @param lottie O LottieAnimationView a ser controlado
     * @param context Contexto para acessar Preferences
     */
    public static void setAnimationState(LottieAnimationView lottie, Context context) {
        if (lottie == null || context == null) return;

        Preferences preferences = new Preferences(context);
        boolean animationEnabled = preferences.isAnimationEnabled();

        if (animationEnabled) {
            lottie.resumeAnimation();
            lottie.playAnimation();
        } else {
            lottie.pauseAnimation();
        }
    }

    /**
     * Aplica o estado de animação em múltiplos Lotties específicos
     * @param view A view raiz onde os Lotties estão
     * @param context Contexto para acessar Preferences
     * @param lottieIds IDs dos Lotties a serem controlados
     */
    public static void setAnimationStateForLotties(View view, Context context, int... lottieIds) {
        if (view == null || context == null || lottieIds == null) return;

        Preferences preferences = new Preferences(context);
        boolean animationEnabled = preferences.isAnimationEnabled();

        for (int lottieId : lottieIds) {
            View lottieView = view.findViewById(lottieId);
            if (lottieView instanceof LottieAnimationView) {
                LottieAnimationView lottie = (LottieAnimationView) lottieView;
                if (animationEnabled) {
                    lottie.resumeAnimation();
                    lottie.playAnimation();
                } else {
                    lottie.pauseAnimation();
                }
            }
        }
    }
}
