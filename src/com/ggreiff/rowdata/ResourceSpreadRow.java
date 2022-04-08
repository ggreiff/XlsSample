package com.ggreiff.rowdata;

import java.util.Date;

public class ResourceSpreadRow {

    public String ProjectId;

    public String ActivityId;

    public String ResourceId;

    public String SpreadUnitType;

    public Double SpreadUnits;

    public Date SpreadPeriodDate;

    public ResourceSpreadRow(String projectID, String activityID, String resourceId, String spreadUnitType, Double spreadUnits, Date spreadPeriodDate) {
        ProjectId = projectID;
        ActivityId = activityID;
        ResourceId = resourceId;
        SpreadUnitType = "Unknown";
        if (spreadUnitType != null || spreadUnitType.length() > 0 ) SpreadUnitType = spreadUnitType;
        SpreadUnits = 0.0;
        if (spreadUnits != null) SpreadUnits = spreadUnits;
        SpreadPeriodDate = spreadPeriodDate;
    }

    public String getProjectId() {
        return ProjectId;
    }

    public void setProjectId(String projectId) {
        ProjectId = projectId;
    }

    public String getResourceId() {
        return ResourceId;
    }

    public void setResourceId(String resourceId) {
        ResourceId = resourceId;
    }

    public String getActivityId() {
        return ActivityId;
    }

    public void setActivityId(String activityId) {
        ActivityId = activityId;
    }

    public String getSpreadUnitType() {
        return SpreadUnitType;
    }

    public void setSpreadUnitType(String spreadUnitType) {
        SpreadUnitType = spreadUnitType;
    }

    public Double getSpreadUnits() {
        return SpreadUnits;
    }

    public void setSpreadUnits(Double spreadUnits) {
        SpreadUnits = spreadUnits;
    }

    public Date getSpreadPeriodDate() { return SpreadPeriodDate; }

    public void setSpreadPeriodDate(Date spreadPeriodDate) {
        SpreadPeriodDate = spreadPeriodDate;
    }
}