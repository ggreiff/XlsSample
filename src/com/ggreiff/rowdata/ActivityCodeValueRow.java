package com.ggreiff.rowdata;

/**
 * Created by ggreiff on 4/28/2015.
 * ActivityCodeValueRow
 */
public class ActivityCodeValueRow {

    public String ActivityID;

    public String ProjectID;

    public String ActivityCodeType;

    public String ActivityDescription;

    public String ActivityCodeValue;

    public String ActivityCodeNode;

    public ActivityCodeValueRow(String projectID, String activityID, String activityCodeType, String activityDescription, String activityCodeValue, String activityCodeNode) {
        ProjectID = projectID;
        ActivityID = activityID;
        ActivityCodeType = activityCodeType;
        ActivityDescription = activityDescription;
        ActivityCodeValue = activityCodeValue;
        ActivityCodeNode = activityCodeNode;
    }

    public String getActivityCodeType() {
        return ActivityCodeType;
    }

    public void setActivityCodeType(String activityCodeType) {
        ActivityCodeType = activityCodeType;
    }

    public String getActivityDescription() {
        return ActivityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        ActivityDescription = activityDescription;
    }

    public String getActivityCodeValue() {
        return ActivityCodeValue;
    }

    public void setActivityCodeValue(String activityCodeValue) {
        ActivityCodeValue = activityCodeValue;
    }

    public String getActivityCodeNode() {
        return ActivityCodeNode;
    }

    public void setActivityCodeNode(String activityCodeNode) {
        ActivityCodeNode = activityCodeNode;
    }

    public String getProjectID() {
        return ProjectID;
    }

    public void setProjectID(String projectID) {
        ProjectID = projectID;
    }

    public String getActivityID() {
        return ActivityID;
    }

    public void setActivityID(String activityID) {
        ActivityID = activityID;
    }


}
