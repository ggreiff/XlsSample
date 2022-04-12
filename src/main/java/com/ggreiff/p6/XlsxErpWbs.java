package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxErpWbsHelper;
import com.ggreiff.rowdata.ErpWbsRow;
import com.primavera.common.value.ObjectId;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.enm.ActivityType;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.WBS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggreiff on 5/21/2015.
 * XlsWbs
 */
public class XlsxErpWbs {

    final static Logger P6logger =  LogManager.getLogger(XlsxErpWbs.class);

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxErpWbs");

        try {

            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

            XlsxErpWbsHelper xlsxErpWbsHelper = new XlsxErpWbsHelper(commandArgs);
            List<ErpWbsRow> erpWbsRows = xlsxErpWbsHelper.getWbsRows();
            List<String> projectIdList = this.getUniqueProjectIdList(erpWbsRows);

            String wbsName = "Name_ERP";
            String summaryActivityName = "ERP Summary Activity";

            for (String projectId : projectIdList) {
                Project project = null;

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", projectId);
                Project[] projects = elm.loadProjects(Project.getMainFields(), projectWhere, null).getAll();
                if (projects.length == 1) project = projects[0];
                if (project == null) continue;

                P6logger.info(String.format("Processing %s", project.getName()));

                Integer summaryCounter = 0;
                String summaryActivityWhere = String.format(" Name = '%s' ", summaryActivityName);
                Activity[] summaryActivites = project.loadAllActivities(new String[]{"Id", "Name", "Type"}, summaryActivityWhere, "Id desc").getAll();
                if (summaryActivites.length > 0) summaryCounter = getSummaryCount(summaryActivites[0].getId());


                for (ErpWbsRow erpWbsRow : erpWbsRows) {
                    if (!erpWbsRow.getProjectID().equals(projectId)) continue;
                    List<String> wbsRowHierarchy = erpWbsRow.getWbsRowHierarchy();
                    if (wbsRowHierarchy.size() == 0) continue;
                    WBS parentWbs = null;
                    String codeBuild = "";
                    for (int i = 0; i < wbsRowHierarchy.size(); i++) {
                        codeBuild = codeBuild + "." + wbsRowHierarchy.get(i);
                        P6logger.info(String.format("codeBuild %s", codeBuild));
                        WBS wbs = null;
                        String whereWbs = String.format(" ProjectObjectId = '%s' and Code = '%s' ", project.getObjectId(), wbsRowHierarchy.get(i));
                        if (parentWbs != null)
                            whereWbs = String.format("%s and ParentObjectId = '%s' ", whereWbs, parentWbs.getObjectId());
                        WBS[] wbses = elm.loadWBS(WBS.getMainFields(), whereWbs, null).getAll();
                        if (wbses.length == 1) wbs = wbses[0];

                        // if null we need to add it
                        if (wbs == null) {
                            wbs = new WBS(session);
                            wbs.setCode(wbsRowHierarchy.get(i));
                            wbs.setProjectObjectId(project.getObjectId());
                            wbs.setName(String.format("Name%s_%s", i + 1, erpWbsRow.getWbsRowNode()));
                            ObjectId objectId = wbs.create();
                            wbs.setObjectId(objectId);
                            wbs.update();
                            P6logger.info(String.format("new wbs %s", wbs.getName()));
                        }

                        if (i == (wbsRowHierarchy.size() - 1)) {
                            wbs.setName(erpWbsRow.getWbsRowName());
                        }

                        if (parentWbs != null) {
                            wbs.setParentObjectId(parentWbs.getObjectId());
                        }
                        wbs.update();
                        parentWbs = wbs;
                    }
                }

                //
                // Now add our ERP WBS Node and summary activity
                //
                String whereWbs = String.format(" ProjectObjectId = '%s' and Name <> '%s' ", project.getObjectId(), wbsName);
                WBS[] projectWbs = elm.loadWBS(WBS.getMainFields(), whereWbs, null).getAll();
                for (WBS parentWbs : projectWbs) {
                    //
                    // Add Erp summary WBS element
                    //
                    if (parentWbs.getParentObjectId() == null) continue;

                    //
                    // See if we already added the ERP WBS
                    //
                    WBS erpWbs = null;
                    String whereErpWbs = String.format(" Name = '%s' ", wbsName);
                    WBS[] erpWbses = parentWbs.loadWBSChildren(WBS.getMainFields(), whereErpWbs, null).getAll();
                    if (erpWbses.length == 1) erpWbs = erpWbses[0];

                    if (erpWbs == null) {
                        erpWbs = new WBS(session);
                        erpWbs.setCode("ERP");
                        erpWbs.setProjectObjectId(project.getObjectId());
                        erpWbs.setName(wbsName);
                        ObjectId objectId = erpWbs.create();
                        erpWbs.setObjectId(objectId);
                        erpWbs.setParentObjectId(parentWbs.getObjectId());
                        erpWbs.update();
                        P6logger.info(String.format("ERP WBS %s", erpWbs.getName()));
                    }

                    //
                    // See if we already added the ERP Summary Activity
                    //
                    Boolean summaryActivityNeeded = true;
                    for (Activity activitySearch : summaryActivites) {
                        if (!activitySearch.getWBSObjectId().equals(parentWbs.getObjectId())) continue;
                        summaryActivityNeeded = false;
                        break;
                    }
                    if (summaryActivityNeeded) {
                        Activity summaryActivity = new Activity(session);
                        summaryActivity.setId(buildSummaryId(++summaryCounter));
                        summaryActivity.setName(summaryActivityName);
                        summaryActivity.setProjectObjectId(project.getObjectId());
                        summaryActivity.setWBSObjectId(parentWbs.getObjectId());
                        summaryActivity.setType(ActivityType.WBS_SUMMARY);
                        ObjectId objectId = summaryActivity.create();
                        summaryActivity.setObjectId(objectId);
                        summaryActivity.update();
                        P6logger.info(String.format("ERP Summary Activity %s", summaryActivity.getName()));
                    }
                }
            }

        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxErpWbs");
    }

    private List<String> getUniqueProjectIdList(List<ErpWbsRow> wbsList) {
        List<String> retVal = new ArrayList<>();
        for (ErpWbsRow erpWbsRow : wbsList) {
            if (retVal.contains(erpWbsRow.getProjectID())) continue;
            retVal.add(erpWbsRow.getProjectID());
        }
        return retVal;
    }

    private Integer getSummaryCount(String activityId) {
        try {
            return Integer.parseInt(activityId.replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            return 1000001;
        }
    }

    private String buildSummaryId(Integer sumamryCount) {
        return String.format("ERP%05d", sumamryCount);
    }
}