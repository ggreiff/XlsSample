package com.ggreiff.rowdata;


import com.primavera.integration.client.bo.enm.UDFDataType;

public class UserFieldActivityRow {


    public UserFieldActivityRow(String projectId, String activityId, String title, String userType, String userValue){
        ProjectId = projectId;
        ActivityId = activityId;
        Title = title;
        UserType = userType;
        UserValue = userValue;
    }

    public String getProjectId() {
        return ProjectId;
    }

    public void setProjectId(String projectId) {
        ProjectId = projectId;
    }

    public String getActivityId() {
        return ActivityId;
    }

    public void setActivityId(String activityId) {
        ActivityId = activityId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getUserType() {
        return UserType;
    }

    public void setUserType(String userType) {
        UserType = userType;
    }

    public String getUserValue() { return UserValue; }

    public void setUserValue(String userValue) {
        UserValue = userValue;
    }

    public String ProjectId;

    public String ActivityId;

    public String Title;

    public String UserType;

    public String UserValue;


    public UDFDataType getUdfDataType(String dataType){

        if (dataType.equalsIgnoreCase("CODE")) return UDFDataType.CODE;
        if (dataType.equalsIgnoreCase("COST")) return UDFDataType.COST;
        if (dataType.equalsIgnoreCase("DOUBLE")) return UDFDataType.DOUBLE;
        if (dataType.equalsIgnoreCase("FINISH_DATE")) return UDFDataType.FINISH_DATE;
        if (dataType.equalsIgnoreCase("INDICATOR")) return UDFDataType.INDICATOR;
        if (dataType.equalsIgnoreCase("INTEGER")) return UDFDataType.INTEGER;
        if (dataType.equalsIgnoreCase("NULL")) return UDFDataType.NULL;
        if (dataType.equalsIgnoreCase("START_DATE")) return UDFDataType.START_DATE;
        if (dataType.equalsIgnoreCase("TEXT")) return UDFDataType.TEXT;
        return UDFDataType.NULL;
    }

}
