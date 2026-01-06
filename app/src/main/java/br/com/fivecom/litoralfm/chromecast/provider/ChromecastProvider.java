package br.com.fivecom.litoralfm.chromecast.provider;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.ImageHints;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.common.images.WebImage;

import java.util.List;

import br.com.fivecom.litoralfm.R;
import br.com.fivecom.litoralfm.chromecast.expanded.ExpandedControlsActivity;
import br.com.fivecom.litoralfm.utils.constants.Constants;

public class ChromecastProvider implements OptionsProvider {

    @NonNull
    @Override
    public CastOptions getCastOptions(Context context) {

        /*NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(Arrays.asList(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING), new int[]{0, 1})
                .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
                .build();*/

        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setImagePicker(new ImagePickerImpl())
                //.setNotificationOptions(notificationOptions)
                .setMediaIntentReceiverClassName(context.getString(R.string.app_name))
                .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
                .build();

        return new CastOptions.Builder()
                .setReceiverApplicationId(Constants.data.id_google_cast)
                .setLaunchOptions(new LaunchOptions.Builder().setRelaunchIfRunning(true).build())
                .setEnableReconnectionService(true)
                .setCastMediaOptions(mediaOptions)
                .setStopReceiverApplicationWhenEndingSession(true)
                .build();
    }

    //Sessions
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

    //Get Image
    private static class ImagePickerImpl extends ImagePicker {
        @Override
        public WebImage onPickImage(MediaMetadata mediaMetadata, @NonNull ImageHints imageHints) {
            if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
                return null;
            }
            List<WebImage> images = mediaMetadata.getImages();
            if (images.size() == 1) {
                return images.get(0);
            } else {
                if (imageHints.getType() == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    return images.get(0);
                } else {
                    return images.get(1);
                }
            }
        }
    }

}
