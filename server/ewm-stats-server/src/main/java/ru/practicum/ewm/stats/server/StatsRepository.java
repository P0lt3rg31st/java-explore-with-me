package ru.practicum.ewm.stats.server;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<Hit, Long> {
}
