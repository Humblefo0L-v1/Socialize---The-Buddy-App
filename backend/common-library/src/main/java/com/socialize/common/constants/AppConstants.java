package com.socialize.common.constants;

public class AppConstants {
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIRECTION = "asc";
    public static final int MAX_PAGE_SIZE = 50;
    
    // JWT Constants
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";
    public static final long JWT_EXPIRATION_MS = 86400000; // 24 hours
    public static final long JWT_REFRESH_EXPIRATION_MS = 604800000; // 7 days
    
    // Event Status
    public static final String EVENT_STATUS_UPCOMING = "UPCOMING";
    public static final String EVENT_STATUS_ONGOING = "ONGOING";
    public static final String EVENT_STATUS_COMPLETED = "COMPLETED";
    public static final String EVENT_STATUS_CANCELLED = "CANCELLED";
    
    // Participant Status
    public static final String PARTICIPANT_STATUS_PENDING = "PENDING";
    public static final String PARTICIPANT_STATUS_APPROVED = "APPROVED";
    public static final String PARTICIPANT_STATUS_DECLINED = "DECLINED";
    public static final String PARTICIPANT_STATUS_LEFT = "LEFT";
    
    // Geolocation
    public static final double DEFAULT_SEARCH_RADIUS_KM = 10.0;
    public static final double MAX_SEARCH_RADIUS_KM = 100.0;
    
    // Cache Keys
    public static final String CACHE_USER_PREFIX = "user:";
    public static final String CACHE_EVENT_PREFIX = "event:";
    public static final String CACHE_LOCATION_PREFIX = "location:";
    public static final int CACHE_TTL_SECONDS = 3600; // 1 hour
}
