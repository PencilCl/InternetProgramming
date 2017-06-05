package cn.pencilsky.mimeviewer;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NetworkClient.OnDownloadedListener {
    TextView urlText; // url输入框
    Button button; // 查看按钮
    TextView filenameText; // 下载文件名
    TextView fileLenText; // 下载文件大小
    TextView downloadedLenText; // 已下载文件比例
    ProgressBar progressBar; // 进度条
    MIMEViewerView mimeViewerView; // MIME查看器

    NetworkClient networkClient;

    String errorInfo;
    String sdPath;

    DecimalFormat decimalFormat = new DecimalFormat("0.00"); // 保留两位小数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        urlText = (TextView) findViewById(R.id.urlText);
        filenameText = (TextView) findViewById(R.id.filenameText);
        fileLenText = (TextView) findViewById(R.id.fileLenText);
        downloadedLenText = (TextView) findViewById(R.id.downloadedLenText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mimeViewerView = (MIMEViewerView) findViewById(R.id.mimeViewer);

        sdPath = Environment.getExternalStorageDirectory() + "/download/";
    }

    @Override
    public void onClick(View view) {
        if ("查看".equals(button.getText())) {
            networkClient = new NetworkClient(urlText.getText().toString(), sdPath);
            networkClient.setOnDownloadedListener(this);
            networkClient.start();
            progressBar.setProgress(0);
            button.setText("取消下载");
        } else if ("取消下载".equals(button.getText())) {
            networkClient.stopDownload();
            button.setText("正在取消下载...");
        }
    }

    @Override
    public void onGetResInfo(final String filename, final int len) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filenameText.setText(filename);
                fileLenText.setText(String.valueOf(len) + "B");
                progressBar.setMax(len);
            }
        });
    }

    @Override
    public void onDownloaded(final int len) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int currentLen = progressBar.getProgress() + len;
                progressBar.setProgress(currentLen);
                downloadedLenText.setText("已下载： " + decimalFormat.format((currentLen * 100.0 / progressBar.getMax())) + "%");
            }
        });
    }

    @Override
    public void onError(final String errorInfo) {
        this.errorInfo = errorInfo;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, MainActivity.this.errorInfo, Toast.LENGTH_SHORT).show();
                button.setText("查看");
            }
        });
    }

    @Override
    public void onFinished(final String filename, final String mime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progressBar.getMax());
                downloadedLenText.setText("已下载： 100.00%");
                button.setText("查看");

                mimeViewerView.display(filename, mime);
            }
        });
    }
}
