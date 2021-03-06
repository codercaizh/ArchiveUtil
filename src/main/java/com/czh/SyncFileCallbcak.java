package com.czh;

import java.io.File;

/**
 * 同步状态回调
 */
public interface SyncFileCallbcak {
    void call(File srcFile,File desFile,SyncStatus status);
}


