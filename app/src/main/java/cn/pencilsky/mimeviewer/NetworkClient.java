package cn.pencilsky.mimeviewer;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlin on 03/06/2017.
 */
public class NetworkClient extends Thread {
    private String url;
    private String sdPath;
    private String mimeType;
    private OnDownloadedListener onDownloadedListener;

    static String[] mimeArray = {"video/x-msvideo", "video/mp4", "text/html", "text/plain", "text/css", "image/bmp", "image/gif", "image/jpeg", "application/msword", "application/x-javascript"};
    static String[] extArray = {"avi", "mp4", "html", "txt", "css", "bmp", "gif", "jpeg", "doc", "js"};

    //MD5的字符串常量
    private final static String[] hexDigits = { "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };


    private boolean exit;

    public NetworkClient(String url, String sdPath) {
        this.url = url;
        this.sdPath = sdPath;

        this.exit = false;
    }

    public void setOnDownloadedListener(OnDownloadedListener onDownloadedListener) {
        this.onDownloadedListener = onDownloadedListener;
    }

    @Override
    public void run() {
        try {
            URL urlObj = new URL(url);
            URLConnection urlConnection = urlObj.openConnection();

            String filename = getFileName(urlObj);
            if (filename == null) {
                if (onDownloadedListener != null) {
                    onDownloadedListener.onError("获取文件名失败");
                }
                return ;
            }

            File file = new File(sdPath);
            if (!file.exists()) {
                file.mkdir();
            }
            String ext = getExt(urlConnection.getContentType());
            if (ext != null && !filename.endsWith(ext)) {
                filename += "." + ext;
            }

            file = new File(file, filename);
            InputStream is = urlConnection.getInputStream();
            FileOutputStream fos = new FileOutputStream(file);

            if (onDownloadedListener != null) {
                onDownloadedListener.onGetResInfo(filename, urlConnection.getContentLength());
            }

            byte[] bytes = new byte[1024];
            int len;
            while ((len = is.read(bytes, 0, 1024)) > 0 && !exit) {
                fos.write(bytes, 0, len);
                if (onDownloadedListener != null) {
                    onDownloadedListener.onDownloaded(len);
                }
            }

            if (onDownloadedListener != null) {
                if (exit) {
                    onDownloadedListener.onError("暂停下载");
                } else {
                    onDownloadedListener.onFinished(file.getAbsolutePath(), mimeType);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            if (onDownloadedListener != null) {
                onDownloadedListener.onError("url 格式错误");
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (onDownloadedListener != null) {
                onDownloadedListener.onError("下载失败");
            }
        }
    }

    public void stopDownload() {
        this.exit = true;
    }

    public interface OnDownloadedListener {
        void onGetResInfo(String filename, int len);
        void onDownloaded(int len);
        void onError(String errorInfo);
        void onFinished(String filename, String mimeType);
    }

    /**
     * 根据content type获取拓展名
     * @param contentType
     * @return 失败返回null
     */
    private String getExt(String contentType) {
        if (contentType == null) return null;

        for (int i = 0; i < mimeArray.length; ++i) {
            if (contentType.contains(mimeArray[i])) {
                mimeType = mimeArray[i];
                return extArray[i];
            }
        }

        mimeType = null;
        return null;
    }

    /**
     * 从url中获取文件名
     * @param urlObj
     * @return 若url中不存在文件名，则返回一个随机md5序列作为文件名
     */
    private String getFileName(URL urlObj) {
        String path = urlObj.getPath();
        int index = path.lastIndexOf('/');
        if (index != -1) {
            path = path.substring(index + 1);
            try {
                path = URLDecoder.decode(path, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }

            String reg = "(.+)\\.([a-z]+)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(path);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            StringBuilder sb = new StringBuilder();
            byte[] bytes = md.digest(new Date().toString().getBytes());
            for (int i = 0;i < bytes.length; ++i) {
                sb.append(byteToHexString(bytes[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
