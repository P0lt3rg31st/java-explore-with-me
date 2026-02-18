package ru.practicum.ewm.stats.server.mapper;

public interface StatsView {
    String getApp();
    String getUri();
    Long getHits();
}