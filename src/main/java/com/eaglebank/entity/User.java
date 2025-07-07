package com.eaglebank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Entity
@Table(name = "users")//avoids reserved word conflicts
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    private String name;

    @Column(unique = true, nullable = false)
    private String email;


    @Column(nullable = false)
    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    private String phoneNumber;

    @Embedded
    private Address address;

    private LocalDateTime createdTimestamp;
    @Column(name = "updated_timestamp")
    private LocalDateTime updatedTimestamp;

    @PrePersist
    public void prePersist(){
        this.createdTimestamp = LocalDateTime.now();
        this.updatedTimestamp = this.createdTimestamp;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedTimestamp = LocalDateTime.now();
    }
}
