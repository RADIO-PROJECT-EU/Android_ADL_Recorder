package radio.gstavrinos.android_adl_recorder;

import android.content.Context;

/**
 * Created by gstavrinos on 9/8/17.
 */

public class DisplayHelper {
    private static Float scale;
    public static int dpToPixel(int dp, Context context) {
        if (scale == null)
            scale = context.getResources().getDisplayMetrics().density;
        return (int) ((float) dp * scale);
    }
}
