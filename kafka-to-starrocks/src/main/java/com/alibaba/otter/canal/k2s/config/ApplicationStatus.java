package com.alibaba.otter.canal.k2s.config;


/**
 * 应用状态
 * @author mujingjing
 * @date 2024/2/5
 **/
public class ApplicationStatus {

    private ApplicationStatus() {
    }

    private static final class ApplicationStatusHolder {
        static final ApplicationStatus APPLICATION_STATUS = new ApplicationStatus();
    }

    public static ApplicationStatus getApplicationStatus(){
        return ApplicationStatusHolder.APPLICATION_STATUS;
    }

    private final boolean isRegister = false;

    public boolean isRegister() {
        return ApplicationStatusHolder.APPLICATION_STATUS.isRegister;
    }

    public synchronized void setRegister(boolean register) {
        ApplicationStatusHolder.APPLICATION_STATUS.setRegister(register);
    }
}
