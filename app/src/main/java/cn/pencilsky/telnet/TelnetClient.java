package cn.pencilsky.telnet;

import java.io.*;
import java.net.Socket;

/**
 * Created by chenlin on 05/06/2017.
 */
public class TelnetClient {
    // 方向键
    public final static byte[] UP = {(byte) 0x1b, (byte) 0x5b, (byte) 0x41};
    public final static byte[] DOWN = {(byte) 0x1b, (byte) 0x5b, (byte) 0x42};
    public final static byte[] LEFT = {(byte) 0x1b, (byte) 0x5b, (byte) 0x44};
    public final static byte[] RIGHT = {(byte) 0x1b, (byte) 0x5b, (byte) 0x43};

    private Socket socket;
    private InputStreamReader isr;
    private InputStream is;
    private OutputStream os;

    private OnReceiveListener onReceiveListener;

    public TelnetClient(String host) throws IOException {
        this(host, 23);
    }

    public TelnetClient(String host, int port) throws IOException {
        socket = new Socket(host, port);
        is = socket.getInputStream();
        isr = new InputStreamReader(is);
        os = socket.getOutputStream();
    }

    public void send(String text) throws IOException {
        sendBytes(text.getBytes());
        sendEnter();
    }

    public void sendEnter() throws IOException {
        os.write('\r');
        os.flush();
    }

    public void sendBytes(byte[] bytes) throws IOException {
        os.write(bytes);
        os.flush();
    }

    public void beginReceive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                char[] chars = new char[1024];
                int len;
                try {
                    while ((len = isr.read(chars)) != -1) {
                        if (onReceiveListener != null) {
                            onReceiveListener.onReceive(chars, len);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setOnReceiveListener(OnReceiveListener onReceiveListener) {
        this.onReceiveListener = onReceiveListener;
    }

    public interface OnReceiveListener {
        void onReceive(char[] chars, int len);
    }

}
