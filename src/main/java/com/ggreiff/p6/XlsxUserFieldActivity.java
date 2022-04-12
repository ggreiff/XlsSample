package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxUserFieldsActivityHelper;
import com.ggreiff.rowdata.UserFieldActivityRow;
import com.ggreiff.utils.Utils;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.GlobalObjectManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.enm.UDFDataType;
import com.primavera.integration.client.bo.enm.UDFSubjectArea;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.UDFType;
import com.primavera.integration.client.bo.object.UDFValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class XlsxUserFieldActivity {

    public XlsxUserFieldActivity() {
    }


    final static Logger P6logger =  LogManager.getLogger(XlsxUserFieldActivity.class);

    private EnterpriseLoadManager elm = null;
    private GlobalObjectManager gob = null;
    private Session thisSession = null;
    private List<UDFType> udfTypeList = null;
    private List<UserFieldActivityRow> userFieldActivityRows = null;
    private List<UDFValue> activityUdfValues = null;

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxUserFieldActivity");

        try {
            thisSession = session;
            elm = session.getEnterpriseLoadManager();
            gob = session.getGlobalObjectManager();

            XlsxUserFieldsActivityHelper xlsxUserFieldsActivityHelper = new XlsxUserFieldsActivityHelper(commandArgs);
            userFieldActivityRows = xlsxUserFieldsActivityHelper.getUserFieldsActivities();

            List<String> projectIdList = getUniqueProjectIdList();
            for (String projectId : projectIdList) {
                Project project = null;

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", projectId);
                Project[] projects = elm.loadProjects(Project.getMainFields(), projectWhere, null).getAll();
                if (projects.length == 1) project = projects[0];
                if (project == null) continue;

                P6logger.info(String.format("Processing %s", project.getName()));

                List<String> activityIdList = this.getUniqueActivityIdList(project.getId());
                if (activityIdList.size() == 0) continue;

                for (String activityId : activityIdList) {

                    Activity activity = null;
                    activityUdfValues = null;
                    String activityWhere = String.format(" Id = '%s' ", activityId);
                    Activity[] activities = project.loadAllActivities(Activity.getWritableFields(), activityWhere, null).getAll();
                    if (activities.length == 1) activity = activities[0];
                    if (activity == null) continue;

                    List<UserFieldActivityRow> activityRowList = getActivityRowList(project.getId(), activity.getId());
                    for (UserFieldActivityRow userFieldActivityRow : activityRowList) {
                        UDFType udfType = getAddUdfTypeByTitle(userFieldActivityRow.getTitle());
                        if (udfType == null || udfType.getDataType() == UDFDataType.NULL) continue;
                        UDFValue udfValue = getActivityUdfValuesByUDFType(activity.getObjectId().toInteger(), userFieldActivityRow);
                        if (udfValue != null) {
                            udfValue = setUdfValueByType(udfValue, userFieldActivityRow);
                            udfValue.update();
                            continue;
                        }
                        udfValue = new UDFValue(thisSession);
                        udfValue.setUDFTypeObjectId(udfType.getObjectId());
                        udfValue.setForeignObjectId(activity.getObjectId());
                        udfValue = setUdfValueByType(udfValue, userFieldActivityRow);
                        udfValue.create();
                    }
                }
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxUserFieldActivity");
    }

    private UDFValue setUdfValueByType(UDFValue udfValue, UserFieldActivityRow userFieldActivityRow) {
        try {
            UDFDataType udfDataType = getUdfDataTypeByTitle(userFieldActivityRow.getTitle());

            if (udfDataType == UDFDataType.COST) {
                udfValue.setCost(Utils.getCostFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

            if (udfDataType == UDFDataType.DOUBLE) {
                udfValue.setDouble(Utils.getDoubleFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

            if (udfDataType == UDFDataType.START_DATE) {
                udfValue.setStartDate(Utils.getStartDateFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

            if (udfDataType == UDFDataType.FINISH_DATE) {
                udfValue.setFinishDate(Utils.getEndDateFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

            if (udfDataType == UDFDataType.TEXT) {
                udfValue.setText(userFieldActivityRow.getUserValue());
                return udfValue;
            }

            if (udfDataType == UDFDataType.INTEGER) {
                udfValue.setInteger(Utils.getIntegerFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

            if (udfDataType == UDFDataType.INDICATOR) {
                udfValue.setIndicator(Utils.getIndicatorFromString(userFieldActivityRow.getUserValue()));
                return udfValue;
            }

        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return udfValue;
    }


    private UDFValue getActivityUdfValuesByUDFType(Integer activityObjectId, UserFieldActivityRow userFieldActivityRow) {
        try {
            if (activityUdfValues == null) activityUdfValues = getActivityUdfValues(activityObjectId);
            UDFType udfType = getAddUdfTypeByTitle(userFieldActivityRow.getTitle());
            Integer udfTypeKey = udfType.getObjectId().getPrimaryKeyObject();
            for (UDFValue udfValue : activityUdfValues) {
                if (udfTypeKey.equals(udfValue.getUDFTypeObjectId().getPrimaryKeyObject())) return udfValue;
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    private List<UDFValue> getActivityUdfValues(Integer foreignObjectId) {
        try {
            String activityWhere = String.format(" ForeignObjectId = %s ", foreignObjectId.toString());
            BOIterator<UDFValue> udfValueBOIterator = elm.loadUDFValues(UDFValue.getAllFields(), activityWhere, null);
            return Arrays.asList(udfValueBOIterator.getAll());
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return new ArrayList<>();
    }

    private UDFType getAddUdfTypeByTitle(String title) {
        try {
            if (udfTypeList == null)
                udfTypeList = Arrays.asList(gob.loadUDFTypes(UDFType.getRequiredCreateFields(), "SubjectArea = 'Activity'", null).getAll());

            UDFDataType uDFDataType = getUdfDataTypeByTitle(title);
            if (uDFDataType == UDFDataType.NULL) {
                P6logger.error(String.format("Unable to create %s UDFDataType returned UDFDataType.NULL", title));
                return null;
            }

            UDFType udfType = getUdfTypeByTitle(title);
            if (udfType != null) return udfType;

            udfType = new UDFType(thisSession);
            udfType.setDataType(uDFDataType);
            udfType.setTitle(title);
            udfType.setSubjectArea(UDFSubjectArea.ACTIVITY);
            gob.createUDFTypes(new UDFType[]{udfType});
            udfTypeList = Arrays.asList(gob.loadUDFTypes(UDFType.getRequiredCreateFields(), "SubjectArea = 'Activity'", null).getAll());

            udfType = getUdfTypeByTitle(title);
            return udfType;

        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    private UDFType getUdfTypeByTitle(String title) {
        try {
            for (UDFType search1 : udfTypeList) {
                if (!title.equalsIgnoreCase(search1.getTitle())) continue;
                return search1;
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    private List<String> getUniqueProjectIdList() {
        List<String> retVal = new ArrayList<>();
        for (UserFieldActivityRow userFieldActivityRow : userFieldActivityRows) {
            if (retVal.contains(userFieldActivityRow.getProjectId())) continue;
            retVal.add(userFieldActivityRow.getProjectId());
        }
        return retVal;
    }

    private List<UserFieldActivityRow> getActivityRowList(String projectId, String activityId) {
        List<UserFieldActivityRow> retVal = new ArrayList<>();
        for (UserFieldActivityRow userFieldActivityRow : userFieldActivityRows) {
            if (!userFieldActivityRow.getProjectId().equals(projectId)) continue;
            if (!userFieldActivityRow.getActivityId().equals(activityId)) continue;
            retVal.add(userFieldActivityRow);
        }
        return retVal;
    }

    private List<String> getUniqueActivityIdList(String projectId) {
        List<String> retVal = new ArrayList<>();
        for (UserFieldActivityRow userFieldActivityRow : userFieldActivityRows) {
            if (!userFieldActivityRow.getProjectId().equals(projectId)) continue;
            if (retVal.contains(userFieldActivityRow.getActivityId())) continue;
            retVal.add(userFieldActivityRow.getActivityId());
        }
        return retVal;
    }

    private UDFDataType getUdfDataTypeByTitle(String title) {
        for (UserFieldActivityRow userFieldActivityRow : userFieldActivityRows) {
            if (title.equalsIgnoreCase(userFieldActivityRow.getTitle()))
                return userFieldActivityRow.getUdfDataType(userFieldActivityRow.getUserType());
        }
        return UDFDataType.NULL;
    }

    private List<String> getUniqueUserTypeTitle() {
        List<String> retVal = new ArrayList<>();
        for (UserFieldActivityRow userFieldActivityRow : userFieldActivityRows) {
            if (retVal.contains(userFieldActivityRow.getTitle())) continue;
            retVal.add(userFieldActivityRow.getTitle());
        }
        return retVal;
    }

}

