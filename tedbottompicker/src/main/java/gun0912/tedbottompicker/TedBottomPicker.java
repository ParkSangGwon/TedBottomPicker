package gun0912.tedbottompicker;

import android.content.Context;
import android.support.v4.app.FragmentManager;

public class TedBottomPicker extends TedBottomSheetDialogFragment {

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private Builder(Context context) {
            super(context);
        }

        public void show(FragmentManager fragmentManager) {
            create().show(fragmentManager);
        }
    }


}
