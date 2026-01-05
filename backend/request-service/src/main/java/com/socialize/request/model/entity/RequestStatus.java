package com.socialize.request.model.entity;

public enum RequestStatus {
    PENDING,        // Waiting for host response
    APPROVED,       // Host approved the request
    DECLINED,       // Host declined the request
    CANCELLED,      // Requester cancelled the request
    EXPIRED,        // Request expired without response
    AUTO_APPROVED   // Automatically approved (if event allows)
}