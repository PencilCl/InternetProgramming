package cn.pencilsky.internetprogramming;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlin on 29/04/2017.
 */
public class Downloader implements DownloadInterface {
    String url;
    String filename;
    int threadSum;
    int finishedThreadSum;
    int downloadedLen;
    Integer fileLen;
    boolean pause;
    HashMap<String, DownloadThread> downloadThreadHashMap;
    HashMap<String, Integer> startPos;
    HashMap<String, Integer> endPos;

    DownloaderInterface downloaderInterface;

    public Downloader(String url, String filename, int threadSum, DownloaderInterface downloaderInterface) {
        this.url = url;
        this.filename = filename;
        this.threadSum = threadSum;
        this.downloaderInterface = downloaderInterface;
        downloadedLen = 0;
        finishedThreadSum = 0;
        fileLen = null;

        downloadThreadHashMap = new HashMap<>();
        startPos = new HashMap<>();
        endPos = new HashMap<>();
    }

    public void start() {
        // 未获取文件长度，则先获取文件长度
        if (fileLen == null) {
            new GetFileInfo().start();
            return ;
        }

        createAndStartThread();
    }

    public void createAndStartThread() {
        downloaderInterface.onUpdateThreadNum(threadSum);
        finishedThreadSum = 0;
        downloaderInterface.onStatusChange("开始下载");
        for (int i = 0; i < threadSum; ++i) {
            DownloadThread downloadThread = new DownloadThread(url, filename, startPos.get("Thread" + i), endPos.get("Thread" + i), this);
            downloadThread.setName("Thread" + i);
            downloadThread.start();
            downloadThreadHashMap.put("Thread" + i, downloadThread);
        }
    }

    public void pause() {
        downloaderInterface.onStatusChange("暂停下载");
        pause = true;
        Set<String> keys = downloadThreadHashMap.keySet();
        for (String key : keys) {
            downloadThreadHashMap.get(key).pause();
        }
    }

    @Override
    public void finish(DownloadThread downloadThread) {
        finishedThreadSum += 1;
        downloaderInterface.onUpdateThreadNum(threadSum - finishedThreadSum);
        if (threadSum == finishedThreadSum) {
            downloaderInterface.onFinish();
        }
    }

    @Override
    public void download(DownloadThread downloadThread, int len) {
        String key = downloadThread.getName();
        startPos.put(key, startPos.get(key) + len);
        downloadedLen += len;
        downloaderInterface.onUpdatePercent((float) downloadedLen / fileLen);
    }

    /**
     * 异步获取url上文件大小以及文件类型
     */
    class GetFileInfo extends Thread {
        @Override
        public void run() {
            try {
                downloaderInterface.onStatusChange("正在获取文件长度...");

                URL urlObj = new URL(url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) urlObj.openConnection();
                fileLen = httpURLConnection.getContentLength();
                filename += "." + getFileType(httpURLConnection.getContentType());
                httpURLConnection.disconnect();

                if (fileLen == -1) {
                    downloaderInterface.onError("获取文件长度失败");
                    return ;
                }

                downloaderInterface.onStatusChange("文件长度:" + fileLen);

                int singleLen = fileLen / threadSum;
                for (int i = 0; i < threadSum; ++i) {
                    startPos.put("Thread" + i, i * singleLen);
                    // 最后一个线程分配剩余字节；
                    endPos.put("Thread" + i, (i == threadSum - 1 ? fileLen : (i + 1) * singleLen - 1));
                }

                createAndStartThread(); // 启动下载线程
            } catch (MalformedURLException e) {
                downloaderInterface.onError("URL格式有误");
                e.printStackTrace();
            } catch (IOException e) {
                downloaderInterface.onError("IOException");
                e.printStackTrace();
            }
        }

        /**
         * 根据Content-Type 获取文件扩展名
         * 支持html、图片、zip、apk格式
         * @return 不支持或找不到文件类型返回null, 成功返回文件扩展名
         */
        public String getFileType(String contentType) {
            String reg = "(text|image)/([^;]+).*";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(contentType);
            if (matcher.find()) {
                return contentType.replaceAll(reg, "$2");
            }

            if (contentType.contains("zip")) {
                return "zip";
            }

            if (contentType.equals("application/vnd.android.package-archive")) {
                return "apk";
            }

            return null;
        }

    }

}
