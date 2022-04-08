package com.ggreiff;

/**
 * Created by ggreiff on 1/23/2015.
 */
public class Assignment {

    public Assignment(String projectID, String activityID, String resourceID, Double unitDouble) {
        ProjectID = projectID;
        ActivityID = activityID;
        ResourceID = resourceID;
        UnitDouble = unitDouble;
    }
    
    public String ProjectID;

    public String ActivityID;
    
    public String ResourceID;
    
    public Double UnitDouble;

    public String getProjectID() {
        return ProjectID;
    }

    public void setProjectID(String projectId) {
        ProjectID = projectId;
    }

    public String getActivityID() {
        return ActivityID;
    }

    public void setActivityID(String activityID) {
        ActivityID = activityID;
    }

    public String getResourceID() {
        return ResourceID;
    }

    public void setResourceID(String resourceID) {
        ResourceID = resourceID;
    }
    
    public Double getDoublePlannedUnits() { return UnitDouble; }

    public void setDoublePlannedUnits(Double unitDouble) {
    	UnitDouble = unitDouble;
    }
}
