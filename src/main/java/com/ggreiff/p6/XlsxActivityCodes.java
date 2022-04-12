package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxActivityCodeHelper;
import com.ggreiff.rowdata.ActivityCodeValueRow;
import com.primavera.common.value.ObjectId;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.GlobalObjectManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.enm.ActivityCodeTypeScope;
import com.primavera.integration.client.bo.helper.ActivityCodeAssignmentHelper;
import com.primavera.integration.client.bo.helper.ActivityCodeHelper;
import com.primavera.integration.client.bo.helper.ActivityCodeTypeHelper;
import com.primavera.integration.client.bo.helper.BOHelperMap;
import com.primavera.integration.client.bo.object.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ggreiff on 4/28/2015.
 * XlsActivityCodes
 */
public class XlsxActivityCodes {

    final static Logger P6logger =  LogManager.getLogger(XlsxActivityCodes.class);

    public String debugString = "";
    public String activtyCodeName = "AlperCsi";

    public void run(Session session, CommandArgs commandArgs) {

        try {

            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();
            GlobalObjectManager gom = session.getGlobalObjectManager();

            XlsxActivityCodeHelper xlsxActivityCodeHelper = new XlsxActivityCodeHelper(commandArgs);
            List<ActivityCodeValueRow> projectActivityCodes = xlsxActivityCodeHelper.getProjectActivityCodesRows();

            List<String> uniqueProjectIds = new ArrayList<>();
            for (ActivityCodeValueRow projectActivityCode : projectActivityCodes) {
                if (uniqueProjectIds.contains(projectActivityCode.getProjectID())) continue;
                uniqueProjectIds.add(projectActivityCode.getProjectID());
            }

            ActivityCodeTypeHelper activityCodeTypeHelper = (ActivityCodeTypeHelper) BOHelperMap.getBOHelper("ActivityCodeType");
            ActivityCodeHelper activityCodeHelper = (ActivityCodeHelper) BOHelperMap.getBOHelper("ActivityCode");
            ActivityCodeAssignmentHelper activityCodeAssignmentHelper = (ActivityCodeAssignmentHelper) BOHelperMap.getBOHelper("ActivityCodeAssignment");

            //
            // Our objects need for adding activity codes
            //
            Project project;
            Activity activity;
            ActivityCodeType activityCodeType;
            ActivityCode activityCode;

            //
            // Loop through each of the projects contained within the spreadsheet
            //
            for (String projectId : uniqueProjectIds) {

                String projectActivityCodeName = String.format("%s", activtyCodeName);

                //
                // Get our project along with a list of the activities
                //
                project = null;
                String projectWhere = String.format(" Id = '%s' ", projectId);
                BOIterator<Project> projectBoi = elm.loadProjects(Project.getAllFields(), projectWhere, null);
                while (projectBoi.hasNext()) { // Should only be one by
                    project = projectBoi.next();
                }
                if (project == null) continue;
                Activity[] projectActivities = project.loadAllActivities(new String[]{"Id", "ObjectId"}, null, null).getAll();

                //
                // Add our project specific activty codetype for the project if its don't exists
                //
                activityCodeType = null;
                List<ActivityCodeType> activityCodeTypeList = new ArrayList<>(
                        Arrays.asList(elm.loadActivityCodeTypes(ActivityCodeType.getAllFields(), String.format("Name = '%s' ", projectActivityCodeName), null).getAll()));
                if (activityCodeTypeList.size() > 0) {

                    for (ActivityCodeType activityCodeTypeFound : activityCodeTypeList) {
                        if (activityCodeTypeFound.getProjectObjectId() == null) continue;
                        if (!(activityCodeTypeFound.getProjectObjectId().equals(project.getObjectId()) &&
                                activityCodeTypeFound.getScope().equals(ActivityCodeTypeScope.PROJECT))) continue;
                        activityCodeType = activityCodeTypeFound;
                        break;
                    }
                }

                //
                // Add activity code type to this project
                //
                if (activityCodeType == null) {
                    activityCodeType = new ActivityCodeType(session);
                    activityCodeType.setName(projectActivityCodeName);
                    activityCodeType.setProjectObjectId(project.getObjectId());
                    activityCodeType.setScope(ActivityCodeTypeScope.PROJECT);
                    ObjectId[] objectIds = gom.createActivityCodeTypes(new ActivityCodeType[]{activityCodeType});
                    activityCodeType = activityCodeTypeHelper.load(session, ActivityCodeType.getAllFields(), objectIds[0]);
                }

                //
                // get all the activity codes of this type for this project
                //
                List<ActivityCode> activityCodeList = new ArrayList<>();
                Collections.addAll(activityCodeList, activityCodeHelper.load(session, activityCodeType, ActivityCode.getAllFields(), null, null).getAll());

                //
                // Build a map to use later for activity code assignments
                //
                Map<String, ObjectId> uniqueActivityCodesMap = xlsxActivityCodeHelper.getUniqueActivityCodesByProject(projectId);

                //
                // Loop through a unique set of activity codes and build up the hierarchy if needed.
                //
                for (String uniqueActivityCode : uniqueActivityCodesMap.keySet()) {
                    debugString = uniqueActivityCode;
                    List<String> codeList = Arrays.asList(uniqueActivityCode.split(Pattern.quote(".")));
                    ObjectId parentActivityCodeObjectId = null;
                    for (int i = 0; i < codeList.size(); i++) {
                        Boolean foundActivityCode = false;
                        for (ActivityCode searchActivityCode : activityCodeList) {
                            if (!searchActivityCode.getCodeValue().equalsIgnoreCase(codeList.get(i)))
                                continue; // code value doesn't match
                            ObjectId searchParentActivityCodeObjectId = null;
                            try {
                                searchParentActivityCodeObjectId = searchActivityCode.getParentObjectId();
                            }
                            catch(Exception ignored) {}
                            if (!(searchParentActivityCodeObjectId == parentActivityCodeObjectId || searchParentActivityCodeObjectId.equals(parentActivityCodeObjectId)))
                                continue; // check parent
                            parentActivityCodeObjectId = searchActivityCode.getObjectId();
                            foundActivityCode = true;
                            break;
                        }

                        if (!foundActivityCode) {
                            activityCode = new ActivityCode(session);
                            activityCode.setCodeTypeObjectId(activityCodeType.getObjectId());
                            activityCode.setCodeValue(codeList.get(i));
                            if (parentActivityCodeObjectId != null)
                                activityCode.setParentObjectId(parentActivityCodeObjectId);
                            parentActivityCodeObjectId = activityCode.create();
                            activityCode.setObjectId(parentActivityCodeObjectId);
                            activityCodeList.add(activityCode);
                        }
                        if (codeList.size() == (i + 1)) {
                            uniqueActivityCodesMap.put(uniqueActivityCode, parentActivityCodeObjectId);
                        }
                    }
                }

                //
                // Now loop through the spreadsheet ActivityCodeValue and due the assignments
                //
                for ( ActivityCodeValueRow activityCodeValue: projectActivityCodes){

                    if (!projectId.equals(activityCodeValue.getProjectID())) continue;

                    // Get our activity
                    activity = null;
                    for(Activity searchActivity : projectActivities){
                        if (!(searchActivity.getId().equals(activityCodeValue.getActivityID()))) continue;
                        activity = searchActivity;//Activity.load(session, Activity.getAllFields(),searchActivity.getObjectId());
                    }
                    if (activity == null)
                        continue;

                    String activityCodeAssignmentWhere = String.format(" ActivityCodeTypeName = '%s' ", activtyCodeName);
                    ActivityCodeAssignment[] activityCodeAssignments =  activityCodeAssignmentHelper.load(session,activity,ActivityCodeAssignment.getAllFields(), activityCodeAssignmentWhere, null).getAll();

                    if (activityCodeAssignments.length > 1) continue;

                    if (uniqueActivityCodesMap.containsKey(activityCodeValue.getActivityCodeValue())){
                        ActivityCodeAssignment projectAssignment = new ActivityCodeAssignment(session);
                        projectAssignment.setActivityObjectId(activity.getObjectId());
                        if (activityCodeAssignments.length == 1) {
                            projectAssignment = activityCodeAssignments[0];
                            projectAssignment.update();
                        } else {
                            projectAssignment.setActivityCodeObjectId(uniqueActivityCodesMap.get(activityCodeValue.getActivityCodeValue()));
                            projectAssignment.create();
                        }
                    }
                }
                P6logger.info("Process next project");
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
    }
}
