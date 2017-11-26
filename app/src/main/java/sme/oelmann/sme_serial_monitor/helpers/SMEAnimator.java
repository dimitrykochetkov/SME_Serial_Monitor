package sme.oelmann.sme_serial_monitor.helpers;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;

public class SMEAnimator {

    public static void animation(View view, int color1, int color2){
        final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(), color1, color2);
        backgroundColorAnimator.setDuration(500);
        backgroundColorAnimator.start();
    }
}
