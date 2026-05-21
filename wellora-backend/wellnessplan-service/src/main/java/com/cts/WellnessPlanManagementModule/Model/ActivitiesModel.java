package com.cts.WellnessPlanManagementModule.Model;




import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activities")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "activityId"
)
public class ActivitiesModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @JsonProperty("description")
    private String activityDescription;



    @ManyToOne
    @JoinColumn(name = "plan_id_fk", referencedColumnName = "planId")
    private WellnessPlanModel plan;




    @Override
    public String toString() {
        return "ActivitiesModel{" +
                "activityId=" + activityId +
                ", activityDescription='" + activityDescription + '\'' +
                '}';
    }
}
