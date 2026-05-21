package com.cts.WellnessPlanManagementModule.Service;

import com.cts.WellnessPlanManagementModule.DTO.RequestDTO.CreateWellnessPlanDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.ActivitiesDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.PatientReponseWithPlansDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.PlanAssignmentDTO;
import com.cts.WellnessPlanManagementModule.DTO.ResponseDTO.WellnessPlanResponseDTO;
import com.cts.WellnessPlanManagementModule.Exception.PatientNotFoundException;
import com.cts.WellnessPlanManagementModule.Exception.PatientPlanNotExistsException;
import com.cts.WellnessPlanManagementModule.Exception.WellNessPlanAlreadyExistsException;
import com.cts.WellnessPlanManagementModule.Model.ActivitiesModel;
//import com.cts.WellnessPlanManagementModule.Model.PatientModel;
import com.cts.WellnessPlanManagementModule.Model.PlanAssignment;
import com.cts.WellnessPlanManagementModule.Model.WellnessPlanModel;
//import com.cts.WellnessPlanManagementModule.Repository.PatientProfileRepository;

import com.cts.WellnessPlanManagementModule.Repository.PlanAssignmentRepository;
import com.cts.WellnessPlanManagementModule.Repository.WellnessPlanRepository;
import com.cts.WellnessPlanManagementModule.client.NotificationClient;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WellnessPlanService
{
    @Autowired
    private WellnessPlanRepository repository;

    @Autowired
    private PlanAssignmentRepository assignmentRepository;

    @Autowired
    private  NotificationClient notificationClient;

//    @Autowired
//    private PatientProfileRepository profileRepository;


    List<WellnessPlanResponseDTO> plans=new ArrayList<>();


    public WellnessPlanResponseDTO addWellnessPlan(CreateWellnessPlanDTO planInfo)
    {


        // check whether the plan with the same name is already present



        if(repository.existsPlanByPlanName(planInfo.getPlanName()))
        {

            log.error("Plan already exists with the given plan name {}",planInfo.getPlanName());
            // plan already exists then throw an exception
            throw new WellNessPlanAlreadyExistsException("Plan Already Exists");
        }
        WellnessPlanModel wellnessPlan=new WellnessPlanModel(); // it represents current wellnessPlan
        wellnessPlan.setPlanName(planInfo.getPlanName());


// run , walk ,jog
        for(String planDescription :planInfo.getActivityNames())
        {
            // it will get each plan descriptions
            ActivitiesModel activity=new ActivitiesModel();
            activity.setActivityDescription(planDescription);
            activity.setPlan(wellnessPlan);

            wellnessPlan.getActivities().add(activity);
            // Since the getActivities() returns the reference right any updation on this will impact the original List only
      log.debug("List is :{}",wellnessPlan.getActivities());
        }
        WellnessPlanModel result= repository.save(wellnessPlan);

        WellnessPlanResponseDTO finalResponse=new WellnessPlanResponseDTO(result);


        log.info("Wellness Plan added successfully for plan  {}",planInfo.getPlanName());

        return finalResponse;

    }





    public WellnessPlanResponseDTO getPlanInfo(Long id) throws Exception
    {

       WellnessPlanModel currentPlan= repository.fetchAllDatasBasedOnId(id).orElse(null);


       // System.out.println("plan calleddddddddddddddddddd"+currentPlan.getPlanName());
       if(currentPlan==null)
       {
           log.warn("Wellness Plan doesnt exists with id {}",id);
           throw new PatientPlanNotExistsException("Plan with a "+id+" Does not exists");
       }

       log.info("Wellness plan retrieved for id {}",id);
       return new WellnessPlanResponseDTO(currentPlan);
    }


    public List<WellnessPlanResponseDTO> fetchAllPlans() throws Exception
    {

        plans.clear(); // clear all the previous data
       List<WellnessPlanModel> datas= repository.fetchAllPlans().orElseThrow(
                ()->{
                    log.warn("Wellness Plan not yet created ");
                 return   new PatientPlanNotExistsException("No Plans are found");
                }
        );


       for(WellnessPlanModel plan:datas)
       {
           plans.add(new WellnessPlanResponseDTO(plan));

       }

       return plans;


    }


    public void assignPlanToPatient(Long patientId,Long planId) throws PatientPlanNotExistsException, PatientNotFoundException {
        System.out.println("assign service called");

        // Prevent duplicate assignment
        assignmentRepository.findByPatientIdAndWellnessPlan_PlanId(patientId, planId)
                .ifPresent(existing -> {
                    throw new WellNessPlanAlreadyExistsException(
                            "Plan " + planId + " is already assigned to patient " + patientId);
                });

        // get WellNess plan details
       WellnessPlanModel wellnessPlan= repository.fetchAllDatasBasedOnId(planId).orElseThrow(

               ()->{
                  return new PatientPlanNotExistsException("Plan not exists with a Given ID");
               }
       );

        PlanAssignment assignment = new PlanAssignment();

        assignment.setPatientId(patientId);

        assignment.setWellnessPlan(wellnessPlan);
        assignment.setAssignedDate(LocalDate.now());
        assignment.setStatus(PlanAssignment.AssignmentStatus.ACTIVE);
        wellnessPlan.getPlanAssignments().add(assignment);





        repository.save(wellnessPlan);



        int pId=Math.toIntExact(patientId);
        int plId=Math.toIntExact(planId);
        System.out.println();
        notificationClient.notifyPlanAssigned(
                pId  , plId, wellnessPlan.getPlanName());

    }




    public List<ActivitiesDTO> getPlanActivities(Long id) throws PatientPlanNotExistsException {


    List<ActivitiesModel> activitiesList=    repository.findActivitiesByPlanId(id).
                orElseThrow(()->new PatientPlanNotExistsException("Patient Plan not found"));

       List<ActivitiesDTO> resultSet=new ArrayList<>();
    for(ActivitiesModel activity:activitiesList)
    {
        resultSet.add(new ActivitiesDTO(activity.getActivityId(),activity.getActivityDescription()));
    }

    return resultSet;
    }






    public WellnessPlanResponseDTO updatePlan(Long planId, CreateWellnessPlanDTO newPlan) throws PatientPlanNotExistsException {

        WellnessPlanModel currentPlan=repository.fetchAllDatasBasedOnId(planId).orElseThrow(()->new PatientPlanNotExistsException("Plan Not Exists "));


      currentPlan.setPlanName(newPlan.getPlanName());
      currentPlan.getActivities().clear(); // delete the old data

      for(String activity :newPlan.getActivityNames())
      {

          ActivitiesModel activitiesModel=new ActivitiesModel();
          activitiesModel.setActivityDescription(activity);
          activitiesModel.setPlan(currentPlan);
          currentPlan.getActivities().add(
                  activitiesModel
          );
      }


      // now currentPlan is filled with new Data

        WellnessPlanModel updatedPlan = repository.save(currentPlan);
        return new WellnessPlanResponseDTO(updatedPlan);


    }


    @Transactional
    public void deleteWellnessPlan(Long planId) throws PatientPlanNotExistsException {
        WellnessPlanModel plan = repository.fetchAllDatasBasedOnId(planId).orElseThrow(() -> new PatientPlanNotExistsException(
                        "Plan not found with ID: " + planId));

        repository.delete(plan);
    }




    public List<PlanAssignmentDTO> getActiveAssignmentsByPatient(
            Long patientId) {
        // log.debug("Fetching active assignments for patientId: {}", patientId);


       List<PlanAssignment> assignments= assignmentRepository.findByPatientIdAndStatus(patientId, PlanAssignment.AssignmentStatus.ACTIVE);

       List<PlanAssignmentDTO> planAssignmentDTOList=new ArrayList<>();
       for(PlanAssignment assignment:assignments)
       {
           planAssignmentDTOList.add(new PlanAssignmentDTO(assignment.getAssignmentId(),assignment.getPatientId(),assignment.getWellnessPlan().getPlanId(),assignment.getWellnessPlan().getPlanName(),assignment.getAssignedDate(),assignment.getStatus().name()));
       }


    return planAssignmentDTOList;
    }


    public List<PlanAssignmentDTO> getActiveAssignmentsByPlan(Long planId) {
        //   log.debug("Fetching active assignments for planId: {}", planId);



        List<PlanAssignment> assignments= assignmentRepository.findByWellnessPlan_PlanIdAndStatus(planId, PlanAssignment.AssignmentStatus.ACTIVE);

        List<PlanAssignmentDTO> planAssignmentDTOList=new ArrayList<>();
        for(PlanAssignment assignment:assignments)
        {
            planAssignmentDTOList.add(new PlanAssignmentDTO(assignment.getAssignmentId(),assignment.getPatientId(),assignment.getWellnessPlan().getPlanId(),assignment.getWellnessPlan().getPlanName(),assignment.getAssignedDate(),assignment.getStatus().name()));
        }
        return planAssignmentDTOList;
    }













}




