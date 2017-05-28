package cn.pencilsky.internetprogramming;

/**
 * Created by chenlin on 29/04/2017.
 */
public interface DownloaderInterface {
    void onUpdateThreadNum(int threadNum);
    void onUpdatePercent(float percent);
    void onFinish();
    void onError(String error);
    void onStatusChange(String status);
}