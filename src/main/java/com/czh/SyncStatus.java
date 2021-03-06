package com.czh;

public enum SyncStatus {
    /**
     * 成功
     */
    SUCCESS("成功√"),

    /**
     * 失败
     */
    FAIL("失败×"),

    /**
     * 忽略
     */
    IGNORE("忽略!");
    private String desc;

    SyncStatus(String desc){
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
