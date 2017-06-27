package jp.ac.titech.itpro.sdl.scrollbycamera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    String TAG = "MainActivity";
    final int REQUEST_SUB = 1000;
    final int RESULT_SUB = 100;

    WebView webView;
    int scrollVertical = 0;
    int scrollHorizontal = 0;
    float alpha, threshold, scale;

    Vector2D vvector = new Vector2D(1, 0);
    Vector2D hvector = new Vector2D(0, -1);

    CameraBridgeViewBase mCameraView;

    MotionDetector detector;

    Timer timer = new Timer();

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

        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.alpha, outValue, true);
        alpha = outValue.getFloat();
        outValue = new TypedValue();
        getResources().getValue(R.dimen.threshold, outValue, true);
        threshold = outValue.getFloat();
        outValue = new TypedValue();
        getResources().getValue(R.dimen.scale, outValue, true);
        scale = outValue.getFloat();
        Log.d(TAG, "alpha = " + alpha);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        float x1 = pref.getFloat(getString(R.string.key_x1), 1);
        float y1 = pref.getFloat(getString(R.string.key_y1), 0);
        float x2 = pref.getFloat(getString(R.string.key_x2), 0);
        float y2 = pref.getFloat(getString(R.string.key_y2), -1);
        Log.d(TAG, "vvector = (" + x1 + ", " + y1 + ")");
        Log.d(TAG, "hvector = (" + x2 + ", " + y2 + ")");
        vvector = new Vector2D(x1, y1);
        hvector = new Vector2D(x2, y2);

        webView = (WebView) findViewById(R.id.web_view);
        //リンクをタップしたときに標準ブラウザを起動させない
        webView.setWebViewClient(new WebViewClient());

        //最初にYahoo! Japanのページを表示する。
        webView.loadUrl("http://www.yahoo.co.jp/");
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);

        mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        //mCameraView.setVisibility(View.GONE);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detector.button = true;
            }
        });

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                webView.scrollBy((int) (scrollHorizontal * scale), (int) (scrollVertical * scale));
            }
        }, 10, 10);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(TAG, "onCameraViewStarted");
        detector = new MotionDetector(width, height, threshold);
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Log.d(TAG, "onCameraFrame");
        //Mat mat = new Mat();
        //Imgproc.threshold(inputFrame.gray(), mat, 20.0, 255.0,
        //        Imgproc.THRESH_BINARY);
        //return mat;

        /*
        image = inputFrame.rgba();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2HSV);
        Core.inRange(image, new Scalar(0, 0, 0), new Scalar(25, 255, 255), image);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGB);
*/

        MotionDetector.Motion motion = detector.onCameraFrame(inputFrame.rgba());
        Vector2D v = new Vector2D(motion.x * scale, motion.y * scale);
        double[] comp = Vector2D.decompose(v, hvector, vvector);
        if (comp[0] == 0) {
            scrollHorizontal = (int) (alpha * scrollHorizontal);
        } else {
            scrollHorizontal = (int) comp[0];
        }
        if (comp[1] == 0) {
            scrollVertical = (int) (alpha * scrollVertical);
        } else {
            scrollVertical = (int) comp[1];
        }
        //scrollVertical = (int) (alpha * scrollVertical + (1 - alpha) * comp[1]);
        //Log.d(TAG, "update scroll value " + horizontalAverage + " " + verticalAverage + " " + comp[0] + " " + comp[1]);


        //webView.scrollBy((int) (scrollHorizontal * scale), (int) (scrollVertical * scale));
        //Log.d(TAG, "scroll = " + scrollVertical + " " + scrollHorizontal);

        return motion.image;
    }
/*
    private static class OpenCVLoaderCallback extends BaseLoaderCallback {
        private final CameraBridgeViewBase mCameraView;
        private OpenCVLoaderCallback(Context context, CameraBridgeViewBase cameraView) {
            super(context);
            mCameraView = cameraView;
        }

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    }
*/
    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mCallBack);
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
        if (id == R.id.action_camera) {
            if (webView.getVisibility() == View.VISIBLE) {
                webView.setVisibility(View.GONE);
                //mCameraView.setVisibility(View.VISIBLE);
            } else {
                webView.setVisibility(View.VISIBLE);
                //mCameraView.setVisibility(View.GONE);
            }
            return true;
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SUB);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SUB || resultCode == RESULT_SUB && data != null) {
            float x1 = data.getFloatExtra(getString(R.string.key_x1), 1);
            float y1 = data.getFloatExtra(getString(R.string.key_y1), 0);
            float x2 = data.getFloatExtra(getString(R.string.key_x2), 0);
            float y2 = data.getFloatExtra(getString(R.string.key_y2), -1);
            Log.d(TAG, "vvector = (" + x1 + ", " + y1 + ")");
            Log.d(TAG, "hvector = (" + x2 + ", " + y2 + ")");
            vvector = new Vector2D(x1, y1).normalization();
            hvector = new Vector2D(x2, y2).normalization();
        }
    }
}
