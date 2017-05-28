package cn.pencilsky.internetprogramming;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloaderInterface {
    private TextView savePathTextView;
    private TextView percentTextView;
    private TextView threadNumTextView;
    private ProgressBar progressBar;
    private EditText urlEditText;
    private Button startButton;
    private Button stopButton;

    String fileSavePath;
    Downloader downloader = null;

    private static int THREAD_SUM = 10; // 下载文件启动线程数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        // 获取下载文件夹目录
        fileSavePath = Environment.getExternalStorageDirectory() + "/download/";
        File file = new File(fileSavePath);
        if (!file.exists()) {
            file.mkdir();
        }

        // 获取组件
        savePathTextView = (TextView) findViewById(R.id.saveText);
        savePathTextView.setText("保存路径：" + fileSavePath);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        percentTextView = (TextView) findViewById(R.id.percent);
        threadNumTextView = (TextView) findViewById(R.id.threadNum);
        urlEditText = (EditText) findViewById(R.id.urlText);

        startButton = (Button) findViewById(R.id.start);
        startButton.setOnClickListener(this);

        stopButton = (Button) findViewById(R.id.stop);
        stopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                startDownload();
                break;
            case R.id.stop:
                stopDownload();
                break;
        }
    }

    private void startDownload() {
        if (downloader == null) {
            String url = urlEditText.getText().toString();
            downloader = new Downloader(url, fileSavePath + getFileNameFromUrl(url), THREAD_SUM, this);
        }

        downloader.start();
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopDownload() {
        if (downloader != null) {
            downloader.pause();
            stopButton.setEnabled(false);
            startButton.setEnabled(true);
        }
    }

    /**
     * 从url中获取文件名
     * @param url
     * @return 若url中不存在文件名，则返回一个随机md5序列作为文件名
     */
    private static String getFileNameFromUrl(String url) {
        int index = url.lastIndexOf('/');
        if (index == -1) return null;

        url = url.substring(index + 1);

        String reg = "(.+)\\.([a-z]+)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(new Date().toString().getBytes());
            return new String(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onUpdateThreadNum(final int threadNum) {
        // 启动UI线程更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                threadNumTextView.setText("线程数:" + threadNum);
            }
        });
    }

    @Override
    public void onUpdatePercent(final float percent) {
        // 启动UI线程更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                percentTextView.setText(percent * 100 + "%");
                progressBar.setProgress((int) (percent * 100));
            }
        });
    }

    @Override
    public void onFinish() {
        downloader = null;
        // 启动UI线程更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "下载完成", 1000).show();
                percentTextView.setText("100%");
                progressBar.setProgress(100);
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onError(final String error) {
        downloader = null;
        // 启动UI线程更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, error, 1000).show();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });
    }

    @Override
    public void onStatusChange(final String status) {
        // 启动UI线程更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, status, 1000).show();
            }
        });
    }
}
