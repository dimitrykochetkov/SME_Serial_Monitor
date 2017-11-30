package sme.oelmann.sme_serial_monitor;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import sme.oelmann.sme_serial_monitor.helpers.SMEAnimator;
import sme.oelmann.sme_serial_monitor.helpers.VersionHelper;

public class WelcomeActivity extends AppCompatActivity {

    private Button bStart;
    private LottieAnimationView lavLogo;
    private TextView tvVersion, tvBuildDate, tvCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainActivity.firstLoad) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            setContentView(R.layout.activity_welcome);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.actionbar);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff000000));
            }

            bStart = findViewById(R.id.bStart);
            lavLogo = findViewById(R.id.lavLogo);
            tvVersion = findViewById(R.id.tvVersion);
            tvBuildDate = findViewById(R.id.tvBuildDate);
            tvCompany = findViewById(R.id.tvCompany);

            VersionHelper versionHelper = new VersionHelper(this, BuildConfig.BUILD_DATE);
            String version = "v. " + versionHelper.getVersion();
            tvVersion.setText(version);
            tvBuildDate.setText(versionHelper.getDate());

            Animator.AnimatorListener al = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    bStart.setVisibility(View.VISIBLE);
                    tvVersion.setVisibility(View.VISIBLE);
                    tvCompany.setVisibility(View.VISIBLE);
                    tvBuildDate.setVisibility(View.VISIBLE);
                    SMEAnimator.animationAlpha(bStart, 0f, 1f);
                    SMEAnimator.animationAlpha(tvVersion, 0f, 1f);
                    SMEAnimator.animationAlpha(tvCompany, 0f, 1f);
                    SMEAnimator.animationAlpha(tvBuildDate, 0f, 1f);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            };

            lavLogo.setAnimation("terminal.json");
            lavLogo.loop(false);
            lavLogo.addAnimatorListener(al);

            bStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startNewWindow(MainActivity.class);
                }
            });
        } else { startNewWindow(MainActivity.class); }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (MainActivity.firstLoad )lavLogo.playAnimation();
    }

    private void startNewWindow(Class cl){
        Intent intent = new Intent(this, cl);
        startActivity(intent);
    }
}
