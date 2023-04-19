package com.example.serverapp.model;

public class Client {

    private String clientPackageName;
    private String clientProcessId;
    private String clientData;
    private String ipcMethod;

    public Client(String clientPackageName, String clientProcessId, String clientData, String ipcMethod) {

        this.clientPackageName = clientPackageName;
        this.clientProcessId = clientProcessId;
        this.clientData = clientData;
        this.ipcMethod = ipcMethod;
    }

    public String getClientData() {
        return clientData;
    }

}