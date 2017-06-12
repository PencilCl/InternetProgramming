import java.io.*;
import java.net.Socket;

/**
 * Created by chenlin on 12/06/2017.
 * 接收客户端请求线程
 */
public class ClientThread extends Thread {
    private Socket client;
    private String root;
    private OnDoneWork onDoneWork;

    public ClientThread(Socket client, String root, OnDoneWork onDoneWork) {
        this.client = client;
        this.root = root;
        this.onDoneWork = onDoneWork;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            DataOutputStream dos = new DataOutputStream(client.getOutputStream());

            StringBuilder request = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 0) {
                    // 一个请求信息结束
                    new HandleRequestThread(dos, root, request.toString()).start();
                    request = new StringBuilder();
                } else {
                    request.append(line + "\n");
                }
            }

            br.close();
            dos.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        onDoneWork.onDownWork();
    }

    interface OnDoneWork {
        void onDownWork();
    }
}
