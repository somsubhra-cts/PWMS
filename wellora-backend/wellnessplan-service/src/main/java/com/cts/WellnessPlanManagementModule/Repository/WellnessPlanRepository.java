package com.cts.WellnessPlanManagementModule.Repository;

import com.cts.WellnessPlanManagementModule.Model.ActivitiesModel;
import com.cts.WellnessPlanManagementModule.Model.WellnessPlanModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WellnessPlanRepository extends JpaRepository<WellnessPlanModel,Long>
{

        @Query("select w from WellnessPlanModel w join fetch w.activities where w.planId=:id")
       Optional<WellnessPlanModel> fetchAllDatasBasedOnId(Long id);


        boolean existsPlanByPlanName(String planName);


        @Query("select w from WellnessPlanModel w join fetch w.activities")
        Optional<List<WellnessPlanModel>> fetchAllPlans();




    @Query("select a from WellnessPlanModel w join w.activities a where w.planId = :planId")
    Optional<List<ActivitiesModel>> findActivitiesByPlanId(@Param("planId") Long planId);


}
