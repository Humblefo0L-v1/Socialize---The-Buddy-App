package com.socialize.chat.model.entity;

public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE,
    AUDIO,
    FILE,
    LOCATION,
    SYSTEM  // For system messages like "User joined", "User left"
}