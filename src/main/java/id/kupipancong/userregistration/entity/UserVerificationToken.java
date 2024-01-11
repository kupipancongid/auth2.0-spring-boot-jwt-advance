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
@Table(name = "user_verification_tokens")
public class UserVerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String token;

    @Column(columnDefinition = "timestamp")
    private LocalDateTime tokenTakenAt;

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
