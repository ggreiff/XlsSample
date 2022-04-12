package com.ggreiff.models;

import com.primavera.common.value.Duration;
import com.primavera.integration.client.bo.enm.RelationshipType;
import java.util.ArrayList;
import java.util.List;

public class XlsxDataRelationship {

    private String activityId;

    private String duration;

    private String relationshipType;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public RelationshipType getRelationshipType() {
        if (isNullOrEmpty(relationshipType)) return null;
        if (relationshipType.equalsIgnoreCase("SF")) return RelationshipType.START_TO_FINISH;
        if (relationshipType.equalsIgnoreCase("SS")) return RelationshipType.START_TO_START;
        if (relationshipType.equalsIgnoreCase("FF")) return RelationshipType.FINISH_TO_FINISH;
        if (relationshipType.equalsIgnoreCase("FS")) return RelationshipType.FINISH_TO_START;
        return null;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public Duration getDuration() {
        if (isNotNumeric(duration)) return null;
        return new Duration(getLagDouble(duration));
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public static List<XlsxDataRelationship> ParseRelationshipCsv(String relationshipTokens) {
        List<XlsxDataRelationship> retVal = new ArrayList<>();
        if (isNullOrEmpty(relationshipTokens.trim())) return null;
        String[] parts = relationshipTokens.trim().split(",");
        if (parts.length == 0) return retVal; // No parts

        for (String part : parts) {
            XlsxDataRelationship xlsxDataRelationship = ParseRelationship(part.trim());
            if (xlsxDataRelationship == null) continue;
            retVal.add(xlsxDataRelationship);
        }
        return retVal;
    }

    public static XlsxDataRelationship ParseRelationship(String relationshipToken) {
        if (isNullOrEmpty(relationshipToken)) return null;
        String[] parts = relationshipToken.split(",");
        if (parts.length != 1) return null; // not a simple relationship string

        parts = relationshipToken.split(":");
        if (parts.length != 2) return null; // need two elements an activity and relationship type and maybe lag

        XlsxDataRelationship xlsxDataRelationship = new XlsxDataRelationship();
        xlsxDataRelationship.setActivityId(parts[0].trim());
        String relationshipType = parts[1].trim().substring(0, 2);
        xlsxDataRelationship.setRelationshipType(relationshipType);
        String lagString = parts[1].trim().replace(relationshipType, "");
        if (lagString.length() == 0) lagString = "0.0";
        xlsxDataRelationship.setDuration(lagString);
        return xlsxDataRelationship;
    }

    public static Double getLagDouble(String lagString) {
        try {
            return Double.parseDouble(lagString);
        } catch (Exception ex) {
            return 0d;
        }
    }

    public static Boolean isNotNullOrEmpty(String strValue){
        return !isNullOrEmpty(strValue);
    }

    public static Boolean isNullOrEmpty(String strValue) {
        return (strValue == null || strValue.trim().isEmpty());
    }

    public static Boolean isNotNumeric(String strNum) {
        return !isNumeric(strNum);
    }

    public static boolean isNumeric(String strNum) {
        if (isNullOrEmpty(strNum)) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
