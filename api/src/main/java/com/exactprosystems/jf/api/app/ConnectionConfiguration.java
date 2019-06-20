package com.exactprosystems.jf.api.app;

import java.util.StringJoiner;

public class ConnectionConfiguration {
    private String jar;
    private String workDirectory;
    private String remoteClassName;
    private int startPort;
    private String remoteHost;
    private int remotePort;

    public String getJar() {
        return jar;
    }

    public void setJar(String jar) {
        this.jar = jar;
    }

    public String getWorkDirectory() {
        return workDirectory;
    }

    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    public String getRemoteClassName() {
        return remoteClassName;
    }

    public void setRemoteClassName(String remoteClassName) {
        this.remoteClassName = remoteClassName;
    }

    public int getStartPort() {
        return startPort;
    }

    public void setStartPort(int startPort) {
        this.startPort = startPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ConnectionConfiguration.class.getSimpleName() + "[", "]")
                .add("jar='" + jar + "'")
                .add("workDirectory='" + workDirectory + "'")
                .add("remoteClassName='" + remoteClassName + "'")
                .add("startPort=" + startPort)
                .add("remoteHost='" + remoteHost + "'")
                .add("remotePort=" + remotePort)
                .toString();
    }
}
