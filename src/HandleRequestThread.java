import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

/**
 * Created by chenlin on 12/06/2017.
 * 处理客户端的GET/HEAD请求
 */
public class HandleRequestThread extends Thread {
    private DataOutputStream dos;
    private String root;
    private String requestStr;
    private static final String templateHtml = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n\t<meta charset=\"UTF-8\">\n\t<title>Document</title>\n</head>\n<body>\n\t\n%s </body>\n</html>";
    private static final String templateGet = "HTTP/1.1 %s\r\nDate: %s\r\nServer: MyServer\r\nConnection: keep-alive\r\nContent-Type: %s\r\nContent-Length: %s\r\n\r\n%s";
    private static final String templateHEAD = "HTTP/1.1 %s\r\nLast-Modified: %s\r\nDate: %s\r\nServer: MyServer\r\nConnection: keep-alive\r\n\r\n";

    byte[] buf = new byte[1024]; // 要发送的数据buf

    public HandleRequestThread(DataOutputStream dos, String root, String request) {
        this.dos = dos;
        this.root = root;
        this.requestStr = request;
    }

    @Override
    public void run() {
        Request request = Request.parseToRequest(requestStr);
        Date date = new Date();
        if (request == null) {
            System.err.println("请求信息解析错误");
            System.err.println(requestStr);
            String body = String.format(templateHtml, "请求信息解析错误\r\n");
            send(String.format(templateGet, "400 Bad Request",  date.toString(), "text/html", body.length(), body));
            return ;
        }

        // 打印请求消息
        System.out.println(String.format("处理请求：%s %s", request.getMethod(), request.getPath()));

        File file = new File(root, request.getPath());
        if (file.isDirectory() || !file.exists()) {
            // 请求文件不存在
            System.err.println(String.format("请求文件 %s 不存在", file.getAbsolutePath()));
            String body = String.format(templateHtml, "404 Not Found\r\n");
            send(String.format(templateGet, "404 Not Found", date.toString(), "text/html", body.length(), body));
            return ;
        }

        if ("GET".equals(request.getMethod())) {
            // 处理GET方法
            synchronized (dos) {
                try {
                    dos.writeBytes(String.format(templateGet, "200 OK", date.toString(), getContentType(file), file.length(), ""));
                    FileInputStream fis = new FileInputStream(file);
                    int len;
                    while ((len = fis.read(buf, 0, 1024)) != -1) {
                        dos.write(buf, 0, len);
                    }
                    fis.close();
                    dos.writeBytes("\r\n");
                    dos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if ("HEAD".equals(request.getMethod())) {
            // 处理HEAD方法
            send(String.format(templateHEAD, "200 OK", (new Date(file.lastModified())).toString(), date.toString()));
        } else {
            // 其他Method返回404 Not found
            String body = String.format(templateHtml, "404 Not Found\r\n");
            send(String.format(templateGet, "404 Not Found", date.toString(), "text/html", body.length(), body));
        }
    }

    /**
     * 获取文件的MIME类型
     * @param file
     * @return
     */
    private String getContentType(File file) {
        if (file.getName().endsWith(".html")) {
            return "text/html";
        }
        if (file.getName().endsWith(".css")) {
            return "text/css";
        }

        try {
            return Files.probeContentType(Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void send(String data) {
        synchronized (dos) {
            try {
                dos.writeBytes(data);
                dos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
