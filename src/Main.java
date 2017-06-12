import java.util.Scanner;

/**
 * Created by chenlin on 12/06/2017.
 */
public class Main {
    public static void main(String[] args) {
        HTTPServer httpServer = new HTTPServer();
        httpServer.responseOnDir("//Users/chenlin/Documents/workspace/html/css3-hover-image-15-animation");

        System.out.println("服务器已启动，输入任意信息停止服务");

        Scanner scanner = new Scanner(System.in);
        scanner.next();
        httpServer.stopServer();
    }
}
