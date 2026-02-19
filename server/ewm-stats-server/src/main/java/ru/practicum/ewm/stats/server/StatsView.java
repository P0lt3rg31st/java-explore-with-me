package ru.practicum.ewm.stats.server;

public interface StatsView {
    String getApp();
    String getUri();
    Long getHits();
}