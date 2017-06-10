package cn.pencilsky.telnet;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TelnetClient.OnReceiveListener {
    TextView displayArea;
    TextView command;

    Button sendButton;

    ImageView enter;
    ImageView up;
    ImageView down;
    ImageView left;
    ImageView right;

    ScrollView scrollView;

    TelnetClient telnetClient;

    static final String host = "bbs.pku.edu.cn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    telnetClient = new TelnetClient(host);
                    telnetClient.setOnReceiveListener(MainActivity.this);
                    telnetClient.beginReceive();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        displayArea = (TextView) findViewById(R.id.displayArea);
        command = (TextView) findViewById(R.id.command);
        sendButton = (Button) findViewById(R.id.send);
        enter = (ImageView) findViewById(R.id.enter);
        up = (ImageView) findViewById(R.id.up);
        down = (ImageView) findViewById(R.id.down);
        left = (ImageView) findViewById(R.id.left);
        right = (ImageView) findViewById(R.id.right);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        sendButton.setOnClickListener(this);
        enter.setOnClickListener(this);
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        vibrate(); // 振动

        switch (view.getId()) {
            case R.id.send:
                new SendThread(command.getText().toString()).start();
                command.setText("");
                break;
            case R.id.up:
                new SendThread(TelnetClient.UP).start();
                break;
            case R.id.down:
                new SendThread(TelnetClient.DOWN).start();
                break;
            case R.id.left:
                new SendThread(TelnetClient.LEFT).start();
                break;
            case R.id.right:
                new SendThread(TelnetClient.RIGHT).start();
                break;
            case R.id.enter:
                sendEnter();
                break;
            default:

        }
    }

    @Override
    public void onReceive(char[] chars, int len) {
        new Receive(chars, len);
    }

    private void sendEnter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    telnetClient.sendEnter();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long [] pattern = {0, 50}; // 停止 开启
        vibrator.vibrate(pattern,-1); //重复两次上面的pattern 如果只想震动一次，index设为-1
    }

    class Receive {
        private char[] chars;
        private int len;

        public Receive(char[] chars, int len) {
            this.chars = chars;
            this.len = len;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb = new StringBuilder();
                    sb.append(displayArea.getText());
                    sb.append(String.valueOf(Receive.this.chars, 0,  Receive.this.len));
                    displayArea.setText(sb.toString().replaceAll("\\[(.*?)m", "")); // 替换终端颜色格式
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN); // 滚动到底部
                }
            });
        }
    }

    class SendThread extends Thread {
        private boolean sendEnter;
        private byte[] bytes;

        public SendThread(String text) {
            this.sendEnter = true;
            this.bytes = text.getBytes();
        }

        public SendThread(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            try {
                telnetClient.sendBytes(bytes);
                if (sendEnter) {
                    telnetClient.sendEnter();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
