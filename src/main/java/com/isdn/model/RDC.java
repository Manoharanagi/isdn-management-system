package com.isdn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rdcs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RDC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rdc_id")
    private Long rdcId;

    @NotBlank(message = "RDC name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region region;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Email(message = "Invalid email format")
    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}