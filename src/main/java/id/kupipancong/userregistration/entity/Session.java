package id.kupipancong.userregistration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, columnDefinition = "char(36)", updatable = false)
    private String id;

    private String localAddress;
    private String remoteAddress;
    @Column(columnDefinition = "timestamp")
    private LocalDateTime loggedOutAt;
    @CreationTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
