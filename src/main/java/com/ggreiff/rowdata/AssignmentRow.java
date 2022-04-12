package com.ggreiff.rowdata;

import java.util.Date;

/**
 * Created by ggreiff on 1/23/2015.
 * AssignmentRow
 */
public class AssignmentRow {

    public AssignmentRow() {

    }

    public AssignmentRow(String projectID, String activityID, String resourceID, String unitType, Double unitDouble, Date periodDate) {
        ProjectID = projectID;
        ActivityID = activityID;
        ResourceID = resourceID;
        UnitType = unitType;
        UnitDouble = unitDouble;
        PeriodDate = periodDate;
    }

    public String ProjectID;

    public String ActivityID;

    public String ResourceID;

    public String UnitType;

    public Double UnitDouble;

    public Date PeriodDate;

    public String Key;

    public String getKey() {
        return String.format("%s|%s|%s", ProjectID, ActivityID, ResourceID);
    }

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

    public String getUnitType() {
        return UnitType;
    }

    public void setUnitType(String unitType) {
        UnitType = unitType;
    }

    public Double getDoublePlannedUnits() { return UnitDouble; }

    public void setDoublePlannedUnits(Double unitDouble) {
        UnitDouble = unitDouble;
    }

    public Date getPeriodDate() {
        return PeriodDate;
    }

    public void setPeriodDate(Date periodDate) {
        PeriodDate = periodDate;
    }

    public static AssignmentRow getKeyIds(String uniqueAssignmentRows) {
        String[] parts = uniqueAssignmentRows.split("\\|");
        if (parts.length != 3) return null;
        AssignmentRow AssignmentRow = new AssignmentRow();
        AssignmentRow.ProjectID = parts[0];
        AssignmentRow.ActivityID = parts[1];
        AssignmentRow.ResourceID = parts[2];
        return AssignmentRow;
    }

}
