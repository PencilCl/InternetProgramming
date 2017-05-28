package cn.pencilsky.internetprogramming;

/**
 * Created by chenlin on 29/04/2017.
 */
public interface DownloadInterface {
    void finish(DownloadThread downloadThread);
    void download(DownloadThread downloadThread, int len);
}
