package ru.practicum.ewm.main.server.compilation;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.ewm.main.server.event.model.Event;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "compilations")
@ToString(exclude = "events")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "pinned", nullable = false)
    private boolean pinned;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "event_id", nullable = false)
    )
    @Builder.Default
    private Set<Event> events = new LinkedHashSet<>();
}