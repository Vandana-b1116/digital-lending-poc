package com.digitallending.llmextraction.entity;

import com.digitallending.llmextraction.model.OfficerAction;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "field_approvals", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"extraction_id", "field_name"})
})
@Data
@NoArgsConstructor
public class FieldApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extraction_id", nullable = false)
    private ExtractionResult extractionResult;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "final_value", length = 500)
    private String finalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "officer_action", nullable = false, columnDefinition = "officer_action")
    private OfficerAction officerAction;

    @Column(name = "officer_notes", columnDefinition = "TEXT")
    private String officerNotes;

    @Column(name = "approved_by")
    private String approvedBy;

    @CreationTimestamp
    @Column(name = "approved_at", nullable = false, updatable = false)
    private LocalDateTime approvedAt;
}
