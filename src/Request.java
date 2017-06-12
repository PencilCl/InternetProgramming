import java.util.HashMap;

/**
 * Created by chenlin on 12/06/2017.
 */
public class Request {
    private String method;
    private String path;
    private String httpVersion;
    private HashMap<String, String> headers;

    private Request() {
        headers = new HashMap<>();
    }

    /**
     * 解析request请求信息
     * @param request
     * @return 格式正确返回Request对象，失败返回null
     */
    public static Request parseToRequest(String request) {
        Request res = new Request();
        String[] lines = request.split("\n");

        // 处理第一行
        String[] info = lines[0].split(" ");
        if (info.length != 3) {
            return null;
        }
        res.method = info[0];
        res.path = info[1];
        res.httpVersion = info[2];

        for (int i = 1; i< lines.length; ++i) {
            int index = lines[i].indexOf(":");
            if (index != -1) {
                res.headers.put(lines[i].substring(0, index).trim(), lines[i].substring(index + 1).trim());
            } else {
                return null;
            }
        }

        return res;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
