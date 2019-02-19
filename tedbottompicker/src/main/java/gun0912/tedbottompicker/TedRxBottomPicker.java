package gun0912.tedbottompicker;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentManager;

import java.util.List;

import io.reactivex.Single;

public class TedRxBottomPicker extends TedBottomSheetDialogFragment {

    public static Builder with(Context context) {
        return new Builder(context);
    }


    public static class Builder extends BaseBuilder<Builder> {

        private Builder(Context context) {
            super(context);
        }

        @Override
        public Builder setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
            throw new RuntimeException("You have to use showSingleImage() method. Or read usage document");
        }

        @Override
        public Builder setOnMultiImageSelectedListener(OnMultiImageSelectedListener onMultiImageSelectedListener) {
            throw new RuntimeException("You have to use showMultiImage() method. Or read usage document");
        }

        public Single<Uri> showSingleImage(FragmentManager fragmentManager) {
            return Single.create(emitter -> {
                onImageSelectedListener = emitter::onSuccess;
                onErrorListener = message -> emitter.onError(new Exception(message));
                create().show(fragmentManager);
            });
        }

        public Single<List<Uri>> showMultiImage(FragmentManager fragmentManager) {
            return Single.create(emitter -> {
                onMultiImageSelectedListener = emitter::onSuccess;
                onErrorListener = message -> emitter.onError(new Exception(message));
                create().show(fragmentManager);
            });
        }
    }

}
