package co.mantara.memenator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Gilang on 05/09/2016.
 * gilangmantara@gmail.com
 */
public class SplashscreenActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_splash);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity( new Intent(SplashscreenActivity.this, MainActivity.class));
                finish();
            }
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        handler.removeCallbacksAndMessages(null);
        super.onBackPressed();
    }
}
