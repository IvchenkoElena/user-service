package aston.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    private int age;
    @Column(name = "created_at")
    private LocalDateTime createdAt= LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

    public User(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }
}
