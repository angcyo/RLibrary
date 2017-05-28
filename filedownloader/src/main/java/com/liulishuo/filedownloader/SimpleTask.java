package com.liulishuo.filedownloader;

/**
 * 只用来保存路径, 参数的传递
 * Created by angcyo on 2017-05-28.
 */

public class SimpleTask implements BaseDownloadTask {

    private String path = "";

    @Override
    public BaseDownloadTask setMinIntervalUpdateSpeed(int minIntervalUpdateSpeedMs) {
        return null;
    }

    @Override
    public BaseDownloadTask setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public BaseDownloadTask setPath(String path, boolean pathAsDirectory) {
        return null;
    }

    @Override
    public BaseDownloadTask setListener(FileDownloadListener listener) {
        return null;
    }

    @Override
    public BaseDownloadTask setCallbackProgressTimes(int callbackProgressCount) {
        return null;
    }

    @Override
    public BaseDownloadTask setCallbackProgressMinInterval(int minIntervalMillis) {
        return null;
    }

    @Override
    public BaseDownloadTask setCallbackProgressIgnored() {
        return null;
    }

    @Override
    public BaseDownloadTask setTag(Object tag) {
        return null;
    }

    @Override
    public BaseDownloadTask setTag(int key, Object tag) {
        return null;
    }

    @Override
    public BaseDownloadTask setForceReDownload(boolean isForceReDownload) {
        return null;
    }

    @Override
    public BaseDownloadTask setFinishListener(FinishListener finishListener) {
        return null;
    }

    @Override
    public BaseDownloadTask addFinishListener(FinishListener finishListener) {
        return null;
    }

    @Override
    public boolean removeFinishListener(FinishListener finishListener) {
        return false;
    }

    @Override
    public BaseDownloadTask setAutoRetryTimes(int autoRetryTimes) {
        return null;
    }

    @Override
    public BaseDownloadTask addHeader(String name, String value) {
        return null;
    }

    @Override
    public BaseDownloadTask addHeader(String line) {
        return null;
    }

    @Override
    public BaseDownloadTask removeAllHeaders(String name) {
        return null;
    }

    @Override
    public BaseDownloadTask setSyncCallback(boolean syncCallback) {
        return null;
    }

    @Override
    public BaseDownloadTask setWifiRequired(boolean isWifiRequired) {
        return null;
    }

    @Override
    public int ready() {
        return 0;
    }

    @Override
    public InQueueTask asInQueueTask() {
        return null;
    }

    @Override
    public boolean reuse() {
        return false;
    }

    @Override
    public boolean isUsing() {
        return false;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isAttached() {
        return false;
    }

    @Override
    public int start() {
        return 0;
    }

    @Override
    public boolean pause() {
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public int getDownloadId() {
        return 0;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public int getCallbackProgressTimes() {
        return 0;
    }

    @Override
    public int getCallbackProgressMinInterval() {
        return 0;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isPathAsDirectory() {
        return false;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String getTargetFilePath() {
        return null;
    }

    @Override
    public FileDownloadListener getListener() {
        return null;
    }

    @Override
    public int getSoFarBytes() {
        return 0;
    }

    @Override
    public int getSmallFileSoFarBytes() {
        return 0;
    }

    @Override
    public long getLargeFileSoFarBytes() {
        return 0;
    }

    @Override
    public int getTotalBytes() {
        return 0;
    }

    @Override
    public int getSmallFileTotalBytes() {
        return 0;
    }

    @Override
    public long getLargeFileTotalBytes() {
        return 0;
    }

    @Override
    public int getSpeed() {
        return 0;
    }

    @Override
    public byte getStatus() {
        return 0;
    }

    @Override
    public boolean isForceReDownload() {
        return false;
    }

    @Override
    public Throwable getEx() {
        return null;
    }

    @Override
    public Throwable getErrorCause() {
        return null;
    }

    @Override
    public boolean isReusedOldFile() {
        return false;
    }

    @Override
    public Object getTag() {
        return null;
    }

    @Override
    public Object getTag(int key) {
        return null;
    }

    @Override
    public boolean isContinue() {
        return false;
    }

    @Override
    public boolean isResuming() {
        return false;
    }

    @Override
    public String getEtag() {
        return null;
    }

    @Override
    public int getAutoRetryTimes() {
        return 0;
    }

    @Override
    public int getRetryingTimes() {
        return 0;
    }

    @Override
    public boolean isSyncCallback() {
        return false;
    }

    @Override
    public boolean isLargeFile() {
        return false;
    }

    @Override
    public boolean isWifiRequired() {
        return false;
    }
}
