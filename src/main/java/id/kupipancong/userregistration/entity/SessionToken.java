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
@Table(name = "session_tokens")
public class SessionToken {
    @Id
    @Column(unique = true, columnDefinition = "char(36)", updatable = false)
    private String id;
    @Column(nullable = false)
    private String accessToken;
    @Column(nullable = false)
    private String refreshToken;
    private String localAddress;
    private String remoteAddress;
    private Boolean loggedOut;
    private Boolean refreshTokenUsed;
    @CreationTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(columnDefinition = "timestamp")
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "session_id", referencedColumnName = "id")
    private Session session;
}
