package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxActivityCodeHelper;
import com.ggreiff.helpers.XlsxErpWbsHelper;
import com.ggreiff.rowdata.ErpWbsRow;
import com.primavera.common.value.BeginDate;
import com.primavera.common.value.EndDate;
import com.primavera.common.value.ObjectId;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.EPS;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.WBS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by ggreiff on 6/6/2015.
 */
public class XlsxWbsActivity {
    final static Logger P6logger =  LogManager.getLogger(XlsxWbsActivity.class);

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxWbsActivity");

        try {

            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

            XlsxErpWbsHelper xlsxErpWbsHelper = new XlsxErpWbsHelper(commandArgs);
            List<ErpWbsRow> erpWbsRows = xlsxErpWbsHelper.getWbsRows();
            List<String> projectIdList = this.getUniqueProjectIdList(erpWbsRows);

            String wbsName = "Name_ERP";

            for (String projectId : projectIdList) {
                Project project = null;

                //
                // Map to our WBS object IDs
                //
                Map<String, ObjectId> wbsObbjectIdMap = new HashMap<>();

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", projectId);
                Project[] projects = elm.loadProjects(Project.getMainFields(), projectWhere, null).getAll();
                if (projects.length == 1) project = projects[0];
                if (project == null) {
                    //
                    // Attach our new project to the top of the EPS structure
                    //
                    ObjectId epsObjectId = getEpsRoot(elm);
                    if (epsObjectId == null) continue;

                    //
                    // Create our new project
                    //
                    project = new Project(session);

                    project.setPlannedStartDate(getStartDate());
                    project.setScheduledFinishDate(getEndDate());
                    project.setName(String.format("Project_%s", projectId));
                    project.setId(projectId);
                    project.setParentEPSObjectId(epsObjectId);
                    project.setObjectId(project.create());
                    project.update();
                }

                P6logger.info(String.format("Processing %s", project.getName()));

                //
                // First build up any missing WBS structure
                //
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

                        if (!wbsObbjectIdMap.containsKey(wbs.getName()))
                            wbsObbjectIdMap.put(wbs.getName(), wbs.getObjectId());
                    }
                }

                //
                // Now add our activity under each WBS structure
                //
                for (ErpWbsRow erpWbsRow : erpWbsRows) {
                    if (!erpWbsRow.getProjectID().equals(projectId)) continue;

                    Activity activity = new Activity(session);
                    activity.setId(erpWbsRow.getActivityID());
                    activity.setName(erpWbsRow.getActivityName());
                    activity.setProjectObjectId(project.getObjectId());
                    activity.setType(erpWbsRow.getActivityType());
                    if (wbsObbjectIdMap.containsKey(erpWbsRow.getWbsRowName()))
                        activity.setWBSObjectId(wbsObbjectIdMap.get(erpWbsRow.getWbsRowName()));
                    ObjectId objectId = activity.create();
                    activity.setObjectId(objectId);
                    activity.update();
                    P6logger.info(String.format("Added Activity %s to %s", activity.getName(), erpWbsRow.getWbsRowName()));
                }
            }

        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxWbsActivity");
    }

    private List<String> getUniqueProjectIdList(List<ErpWbsRow> wbsList) {
        List<String> retVal = new ArrayList<>();
        for (ErpWbsRow erpWbsRow : wbsList) {
            if (retVal.contains(erpWbsRow.getProjectID())) continue;
            retVal.add(erpWbsRow.getProjectID());
        }
        return retVal;
    }

    private ObjectId getEpsRoot(EnterpriseLoadManager elm) {

        try {
            EPS[] eps = elm.loadEPS(EPS.getWritableFields(), null, null).getAll();
            for (EPS ep : eps) {
                if (ep.getParentObjectId() == null) return ep.getObjectId();
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    private BeginDate getStartDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return new BeginDate(cal.getTime());
    }

    private EndDate getEndDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 12); // 11 = december
        cal.set(Calendar.DAY_OF_MONTH, 31); // new years eve
        return new EndDate(cal.getTime());
    }
}