package beautiful.libo.com.beautiful_camera_textureview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Log;
import android.view.TextureView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements Camera.PreviewCallback,TextureView.SurfaceTextureListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private Camera camera;
    private TextureView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tv = new TextureView(this);
        tv.setSurfaceTextureListener(this);
        setContentView(tv);

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable: SurfaceTexture创建");
        camera = Camera.open(1);
        camera.setPreviewCallback(this);
        try {
            camera.setPreviewTexture(tv.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged: SurfaceTexture尺寸改变");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed: SurfaceTexture销毁");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureUpdated: SurfaceTexture创建");
    }

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
        } catch (Exception ex) {
            Log.e("Sys", "Error:" + ex.getMessage());
        }

        if (bmp != null) {
            Log.i(TAG, "onPreviewFrame: 绘制图片");
            Canvas canvas = tv.lockCanvas();
            canvas.drawBitmap(bmp, 0, data.length, new Paint());
            tv.unlockCanvasAndPost(canvas);
        }
    }
}
