package ru.practicum.ewm.main.server.request;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.ewm.dto.request.ParticipationRequestStatus;
import ru.practicum.ewm.main.server.event.model.Event;
import ru.practicum.ewm.main.server.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "participation_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_request", columnNames = {"event_id", "requester_id"})
        }
)
@ToString(exclude = {"event", "requester"})
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private ParticipationRequestStatus status = ParticipationRequestStatus.PENDING;
}