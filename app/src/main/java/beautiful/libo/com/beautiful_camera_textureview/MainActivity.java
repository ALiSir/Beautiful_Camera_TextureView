package beautiful.libo.com.beautiful_camera_textureview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

public class MainActivity extends Activity implements Camera.PreviewCallback, SurfaceHolder.Callback ,CameraAutoFoces.CameraFocusListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private Camera camera;
    private SurfaceView sv;
    private SurfaceHolder sh;
    private GPUImage imagegp;
    CameraAutoFoces cameraAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        sv = (SurfaceView) findViewById(R.id.surfaceView);
        sh = sv.getHolder();
        sh.setKeepScreenOn(true);
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        sh.addCallback(this);
        
        cameraAuto = new CameraAutoFoces(getApplicationContext());
        cameraAuto.setCameraFocusListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.i(TAG, "surfaceChanged: Surface创建");
        camera = Camera.open(1);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setJpegQuality(80);
        camera.setDisplayOrientation(90);
        
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);
        camera.startPreview();
        cameraAuto.onStart();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    
     @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraAuto.onStop();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    
     @Override
    public void onFocus() {
        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if(success){
                    Log.i(TAG, "onAutoFocus: 聚焦成功！");
                    camera.setOneShotPreviewCallback(null);
                }
            }
        });
    }


    @SuppressLint("WrongViewCast")
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.i(TAG, "onPreviewFrame: 预览回调");
        Camera.Size size = camera.getParameters().getPreviewSize();
        Bitmap bmp = null;
        try {
            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            if (image != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                stream.close();
            }

            if (bmp != null) {
                imagegp = new GPUImage(this);
                imagegp.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));
                imagegp.setImage(bmp);
                imagegp.setFilter(new GPUImageSepiaFilter(99));
                Bitmap btms = imagegp.getBitmapWithFilterApplied();

                Log.i(TAG, "onPreviewFrame:正在画");
                Canvas canvas = sh.lockCanvas();
                canvas.setBitmap(btms);
                sh.unlockCanvasAndPost(canvas);
            }
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }
    }
}
