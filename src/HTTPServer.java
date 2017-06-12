import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by chenlin on 12/06/2017.
 */
public class HTTPServer implements Runnable, ClientThread.OnDoneWork {
    private int port;
    private String root;

    private boolean running;
    private boolean stop;

    private ServerSocket serverSocket;

    private Integer sumOfCient;

    public HTTPServer() {
        this(8080);
    }

    public HTTPServer(int port) {
        this.port = port;
        this.running = false;
    }

    /**
     * 启动服务器，并设置响应目录(根)为root
     * @param root
     */
    public void responseOnDir(String root) {
        if (running) return ;

        this.root = root;
        new Thread(this).start();
    }

    /**
     * 停止服务
     */
    public void stopServer() {
        this.stop = true;

        // 服务端创建一个client，以跳出serverSocket.accept()阻塞
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // 启动服务器
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }

        // 初始化变量
        sumOfCient = 0;
        stop = false;
        running = true;
        Socket client;

        //监听客户端请求
        while (!stop) {
            try {
                client = serverSocket.accept();
                synchronized (sumOfCient) {
                    ++sumOfCient;
                }
                new ClientThread(client, root, this).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        running = false;
    }

    /**
     * 计算当前客户端数量
     * 当客户端数量为0时，如果服务器正在停止，则断开服务器连接
     */
    @Override
    public void onDownWork() {
        synchronized (sumOfCient) {
            --sumOfCient;
            if (this.stop) {
                if (sumOfCient == 0) {
                    try {
                        if (serverSocket != null) {
                            serverSocket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("服务器已停止");
                } else {
                    System.out.println("正在停止服务...当前还有" + sumOfCient + "个用户在线");
                }
            }
        }
    }
}
