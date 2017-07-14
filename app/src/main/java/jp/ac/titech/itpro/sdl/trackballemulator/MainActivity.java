package jp.ac.titech.itpro.sdl.trackballemulator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    String TAG = "MainActivity";
    final int REQUEST_SUB = 1000;
    final int RESULT_SUB = 100;

    WebView webView;
    Button back, forward;
    EditText urlView;
    InputMethodManager inputMethodManager; //キーボード表示を制御
    int scrollVertical = 0;
    int scrollHorizontal = 0;
    float inc_alpha, dec_alpha, threshold, scale; // パラメータ

    Vector2D vvector = new Vector2D(1, 0);  // 右方向の単位ベクトル
    Vector2D hvector = new Vector2D(0, -1); // 下方向の単位ベクトル

    MyJavaCameraView mCameraView;

    MotionDetector detector;

    Timer timer;

    Boolean button = false; // debug用


    // OpenCVライブラリのロード
    private BaseLoaderCallback mCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "callback SUCCESS");
                    mCameraView.enableView();
                    break;
                default:
                    Log.d(TAG, "callback DEFAULT");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        getSettings();

        back = (Button) findViewById(R.id.back_button);
        forward = (Button) findViewById(R.id.forward_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) webView.goBack();
            }
        });
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) webView.goForward();
            }
        });

        webView = (WebView) findViewById(R.id.web_view);
        // リンクをタップしたときに標準ブラウザを起動させない
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                back.setEnabled(webView.canGoBack());
                forward.setEnabled(webView.canGoForward());
                urlView.setText(webView.getUrl());
                super.onPageFinished(view, url);
            }
        });

        // 初期画面
        webView.loadUrl("https://www.google.co.jp/?gws_rd=ssl#q=トラックボール");
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);

        mCameraView = (MyJavaCameraView) findViewById(R.id.my_camera_view);
        mCameraView.setCvCameraViewListener(this);
        //mCameraView.setVisibility(View.GONE);

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        urlView = (EditText) findViewById(R.id.url_view);
        urlView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // ボタンが押されてなおかつエンターキーだったとき
                if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    // キーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(urlView.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    // フォーカスをwebviewに
                    webView.requestFocus();
                    // URLに飛ぶ
                    webView.loadUrl(urlView.getText().toString());
                    Log.d(TAG, "enter key");
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");
        autoExposure1sec();
        detector = new MotionDetector(width, height, threshold);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                webView.scrollBy((int) (scrollHorizontal * scale), (int) (scrollVertical * scale));
            }
        }, 10, 10);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(TAG, "onCameraViewStopped");
        timer.cancel();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Log.d(TAG, "onCameraFrame");

        MotionDetector.Motion motion = detector.onCameraFrame(inputFrame.rgba());
        Vector2D v = new Vector2D(motion.x * scale, motion.y * scale);
        double[] comp = Vector2D.decompose(v, hvector, vvector); // ベクトルvをhvector,vvectorの2方向の成分に分解
        // don't scroll horizontally
        if (false) {
            if (comp[0] == 0) {
                scrollHorizontal = (int) ((1 - dec_alpha) * scrollHorizontal);
            } else {
                scrollHorizontal = (int) ((1 - inc_alpha) * scrollHorizontal + inc_alpha * comp[0]);
            }
        }
        if (comp[1] == 0) {
            scrollVertical = (int) ((1-dec_alpha) * scrollVertical);
        } else {
            scrollVertical = (int) ((1-inc_alpha) * scrollVertical + inc_alpha * comp[1]);
        }

        return motion.image;
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mCallBack);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCameraView != null) mCameraView.disableView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_camera:
                if (webView.getVisibility() == View.VISIBLE) {
                    webView.setVisibility(View.GONE);
                    //mCameraView.setVisibility(View.VISIBLE);
                } else {
                    webView.setVisibility(View.VISIBLE);
                    //mCameraView.setVisibility(View.GONE);
                }
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, REQUEST_SUB);
                break;
            case R.id.action_exposure:
                autoExposure1sec();
                break;
            case R.id.action_1:
                detector.select = 1;
                break;
            case R.id.action_2:
                detector.select = 2;
                break;
            case R.id.action_3:
                detector.select = 3;
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getSettings();

        Log.d(TAG, "thre, scale, inc, dec = " + threshold + ", " + scale + ", " + inc_alpha + ", " + dec_alpha);
        if (detector != null) {
            detector.setThreshold(threshold);
        }
    }

    private void getSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.inc_alpha, outValue, true);
        inc_alpha = sharedPreferences.getFloat("inc_alpha", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.dec_alpha, outValue, true);
        dec_alpha = sharedPreferences.getFloat("dec_alpha", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.threshold, outValue, true);
        threshold = sharedPreferences.getFloat("threshold", outValue.getFloat());
        outValue = new TypedValue();
        getResources().getValue(R.dimen.scale, outValue, true);
        scale = sharedPreferences.getFloat("scale", outValue.getFloat());
    }

    // カメラの自動露出補正を1秒間働かせて、使用環境の光量に合わせる
    private void autoExposure1sec() {
        Log.d(TAG, "enable autoExposure");
        final Camera c = mCameraView.getCamera();
        final Camera.Parameters p = c.getParameters();
        p.setAutoExposureLock(false);
        c.setParameters(p);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "disable autoExposure");
                try {
                    p.setAutoExposureLock(true);
                    c.setParameters(p);
                } catch (Exception e) {
                    Log.d(TAG, Log.getStackTraceString(e));
                }
            }
        }, 1000);
    }
}
