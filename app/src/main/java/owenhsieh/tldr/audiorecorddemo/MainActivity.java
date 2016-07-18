package owenhsieh.tldr.audiorecorddemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSIONS_REQUESTS = 1;
    private String FILE_DIR;
    private MiniRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
    }

    private void checkPermissions() {
        int permissionCheck;
        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUESTS);
            return;
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUESTS);
            return;
        }

        setup();
    }

    private void setup() {
        FILE_DIR = getExternalCacheDir().getPath();
        File file = new File(FILE_DIR);
        if (!file.exists()) file.mkdir();

        recorder = new MiniRecorder();

        final View pushToTalkButton = findViewById(R.id.push_to_talk_btn);
        final ImageView pushToTalkImg = (ImageView) findViewById(R.id.push_to_talk_img);
        if (pushToTalkButton != null) {
            pushToTalkButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            recorder.setEnable(true);
                            if (pushToTalkImg != null) {
                                pushToTalkImg.setImageResource(R.drawable.ic_mic);
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (recorder.isEnable()) {
                                recorder.setEnable(false);
                                saveFile();
                            }
                            if (pushToTalkImg != null) {
                                pushToTalkImg.setImageResource(R.drawable.ic_mic_off);
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        recorder.resume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUESTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissions();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recorder != null)
            recorder.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recorder != null)
            recorder.release();
    }

    private void saveFile() {
        ByteBuffer rawBuffer = ByteBuffer.allocate(recorder.getBufferSize());
        recorder.getBuffer(rawBuffer);

        try {
            File file;
            DataOutputStream outputStream;

            /* save raw audio data (pcm) */
            file = new File(FILE_DIR, "example.pcm");
            outputStream = new DataOutputStream(new FileOutputStream(file));
            outputStream.write(rawBuffer.array(), 0, rawBuffer.position());
            outputStream.close();

            /* save wave file (wave header + pcm) */
            file = new File(FILE_DIR, "example.wav");
            outputStream = new DataOutputStream(new FileOutputStream(file));
            WaveHeaderWriter.rawToWave(1, recorder.getCurrentRate(), 16, rawBuffer, outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.clearBuffer();
    }
}
