package ph.STICKnoLOGIC.aerial.trimmer.customVideoViews;

import android.net.Uri;

public interface OnVideoTrimListener {

    void onTrimStarted();

    void getResult(final Uri uri);

    void cancelAction();

    void onError(final String message);
}
