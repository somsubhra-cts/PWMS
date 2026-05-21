package com.cts.WellnessPlanManagementModule.Model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
        name="plan_assignment",
        uniqueConstraints ={
                @UniqueConstraint(
                        columnNames = {"patient_id","plan_id"}
                )
        }
)
@Data
@NoArgsConstructor
@ToString

@JsonIdentityInfo(
        property = "assignmentId",
        generator = ObjectIdGenerators.PropertyGenerator.class
)
public class PlanAssignment
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "assignGenerator")
    @SequenceGenerator(name = "assignGenerator",allocationSize = 5)
    private Long assignmentId;





    @Column(name = "patient_id", nullable = false)

    private Long patientId;


    @ManyToOne
    @JoinColumn(name = "plan_id",referencedColumnName ="planId" )
    private WellnessPlanModel wellnessPlan;

    @Column(nullable = false)
    private LocalDate assignedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    public enum AssignmentStatus {
        ACTIVE, COMPLETED, CANCELLED
    }

}
