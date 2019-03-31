package com.example.neel.researchproject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.Toast;
import android.os.CountDownTimer;

public class MainActivity extends Activity implements Callback {

    @Override
    protected void onDestroy() {
        stopRecording();
        super.onDestroy();
    }
    private final int MY_PERMISSIONS=11;
    private FloatingActionButton fab;
    private FloatingActionButton fab2;
    private EditText editText;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    public MediaRecorder mrec = new MediaRecorder();
    private Camera mCamera=null;
    private Camera.Parameters parameters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_PERMISSIONS);




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode)
        {
            case MY_PERMISSIONS:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    fab=(FloatingActionButton)findViewById(R.id.floatingActionButton);
                    fab2=(FloatingActionButton)findViewById(R.id.floatingActionButton2);
                    editText=(EditText)findViewById(R.id.editText);
                    fab.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            surfaceView.setVisibility(View.VISIBLE);
                            try {
                                fab.setVisibility(View.INVISIBLE);
                                fab2.setVisibility(View.VISIBLE);
                                editText.setVisibility(View.VISIBLE);
                                startRecording();
                                CountDownTimer countDownTimer=new CountDownTimer(10000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        editText.setText("00:"+((10000-millisUntilFinished)/1000));
                                    }

                                    @Override
                                    public void onFinish() {
                                        stopRecording();
                                        fab2.setVisibility(View.INVISIBLE);
                                        editText.setVisibility(View.INVISIBLE);
                                        fab.setVisibility(View.VISIBLE);
                                        surfaceView.setVisibility(View.INVISIBLE);
                                    }
                                }.start();
                            } catch (Exception e) {
                                String message = e.getMessage();
                                Log.i(null, "Problem " + message);
                                mrec.release();
                            }
                        }
                    });
                }
        }


    }

    //@Override
    /*public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 0, 0, "Start");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals("Start")) {
            try {

                startRecording();
                item.setTitle("Stop");

            } catch (Exception e) {

                String message = e.getMessage();
                Log.i(null, "Problem " + message);
                mrec.release();
            }

        } else if (item.getTitle().equals("Stop")) {
            mrec.stop();
            mrec.release();
            mrec = null;
            item.setTitle("Start");
        }

        return super.onOptionsItemSelected(item);
    }*/

    protected void startRecording() throws IOException {
        if (mCamera == null)
            mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        parameters=mCamera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        Rect newRect = new Rect(-5,-5,5, 5);
        Camera.Area focusArea = new Camera.Area(newRect, 1000);
        List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
        focusAreas.add(focusArea);
        parameters.setFocusAreas(focusAreas);
        mCamera.setParameters(parameters);
        String filename;
        String path;

        path = Environment.getExternalStorageDirectory().getAbsolutePath()
                .toString();
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "project");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        Date date = new Date();
        filename = "/project/rec" + date.toString().replace(" ", "_").replace(":", "_")
                + ".mp4";

        // create empty file it must use
        File file = new File(path, filename);

        mrec = new MediaRecorder();
        mrec.setOrientationHint(90);

        mCamera.lock();
        mCamera.unlock();

        // Please maintain sequence of following code.

        // If you change sequence it will not work.
        mrec.setCamera(mCamera);
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //mrec.setAudioSource(MediaRecorder.AudioSource.MIC);
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mrec.setVideoFrameRate(24);
        mrec.setVideoEncodingBitRate(3000000);
        mrec.setVideoSize(1920, 1080);
        //mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(path+filename);
        mrec.prepare();
        mrec.start();

    }

    protected void stopRecording() {

        parameters=mCamera.getParameters();
        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);


        if (mrec != null) {
            mrec.stop();
            mCamera.setParameters(parameters);
            mrec.release();
            mrec=null;
            mCamera.release();

            //mCamera.lock();
        }
        Toast.makeText(getApplicationContext(),"Video successfully stored in the project folder.",Toast.LENGTH_SHORT).show();
    }

    private void releaseMediaRecorder() {

        if (mrec != null) {
            mrec.reset(); // clear recorder configuration
            mrec.release(); // release the recorder object
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.i("Surface", "called");
        mCamera=Camera.open();

        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            mCamera.setParameters(params);
            Log.i("Surface", "Created");
        } else {
            Toast.makeText(getApplicationContext(), "Camera not available!",
                    Toast.LENGTH_LONG).show();

            finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera!=null) {
            //   mCamera.stopPreview();
            // mCamera.release();
        }

    }

}