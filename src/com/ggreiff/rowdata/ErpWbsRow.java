package com.ggreiff.rowdata;

import com.primavera.integration.client.bo.enm.ActivityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ggreiff on 5/21/2015.
 * WbsRow
 */
public class ErpWbsRow {

    public ErpWbsRow() {

    }

    public ErpWbsRow(String projectID, String activityID, String activityName, String activityTypeCode, String wbsRowName, String wbsRowNode) {
        ProjectID = projectID;
        ActivityID = activityID;
        ActivityName = activityName;
        ActivityTypeCode = activityTypeCode;
        WbsRowName = wbsRowName;
        WbsRowNode = wbsRowNode;
    }

    public String ProjectID;

    public String ActivityID;

    public String ActivityName;

    public String ActivityTypeCode;

    public String WbsRowNode;

    public String WbsRowName;

    public String getProjectID() { return ProjectID; }

    public void setProjectID(String projectId) {
        ProjectID = projectId;
    }

    public String getActivityName() {
        return ActivityName;
    }

    public void setActivityName(String activityName) { ActivityName = activityName; }

    public String getActivityID() {
        return ActivityID;
    }

    public void setActivityID(String activityID) {
        ActivityID = activityID;
    }

    public ActivityType getActivityType() {
        if (ActivityTypeCode.equalsIgnoreCase("LEVEL_OF_EFFORT")) return ActivityType.LEVEL_OF_EFFORT;
        if (ActivityTypeCode.equalsIgnoreCase("WBS_SUMMARY")) return ActivityType.WBS_SUMMARY;
        if (ActivityTypeCode.equalsIgnoreCase("MILESTONE")) return ActivityType.MILESTONE;
        if (ActivityTypeCode.equalsIgnoreCase("FINISH_MILESTONE")) return ActivityType.FINISH_MILESTONE;
        if (ActivityTypeCode.equalsIgnoreCase("RESOURCE_DEPENDENT")) return ActivityType.RESOURCE_DEPENDENT;
        if (ActivityTypeCode.equalsIgnoreCase("TASK_DEPENDENT")) return ActivityType.TASK_DEPENDENT;
        return ActivityType.MILESTONE;
    }

    public void setActivityType(String activityTypeCode) {
        ActivityTypeCode = activityTypeCode;
    }

    public String getWbsRowNode() {
        return WbsRowNode;
    }

    public void setWbsRowNode(String wbsRowNode) {
        WbsRowNode = wbsRowNode;
    }

    public String getWbsRowName() {
        return WbsRowName;
    }

    public void setWbsRowName(String wbsRowName) {
        WbsRowName = wbsRowName;
    }

    public List<String> getWbsRowHierarchy() {
        List<String> WbsRowNodeList = new ArrayList<>();
        String[] nodes = getWbsRowNode().split("\\.");
        Collections.addAll(WbsRowNodeList, nodes);
        return WbsRowNodeList;
    }
}