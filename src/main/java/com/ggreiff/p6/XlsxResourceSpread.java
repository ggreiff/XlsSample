package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxResourceSpreadHelper;
import com.ggreiff.rowdata.ResourceSpreadRow;
import com.ggreiff.utils.Utils;
import com.primavera.common.value.*;
import com.primavera.common.value.spread.ResourceAssignmentSpread;
import com.primavera.common.value.spread.ResourceAssignmentSpreadPeriod;
import com.primavera.common.value.spread.SpreadPeriod;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.GlobalObjectManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.enm.SpreadPeriodType;
import com.primavera.integration.client.bo.object.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class XlsxResourceSpread {


    final static Logger P6logger =  LogManager.getLogger(XlsxResourceSpread.class);

    private EnterpriseLoadManager elm;
    private GlobalObjectManager gob;
    private Session thisSession;
    private List<FinancialPeriod> financialPeriods;

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxResourceSpread");
        String debug = "";
        try {
            thisSession = session;
            elm = session.getEnterpriseLoadManager();
            gob = session.getGlobalObjectManager();

            XlsxResourceSpreadHelper xlsxResourceSpreadHelper = new XlsxResourceSpreadHelper(commandArgs);
            List<ResourceSpreadRow> resourceSpreadRows = xlsxResourceSpreadHelper.getResourceSpreadRows();
            List<String> projectIdList = this.getUniqueProjectIdList(resourceSpreadRows);
            String[] nonSpreadFields = new String[]{
                    "ProjectId",
                    "RemainingUnits",
                    "ResourceId",
                    "ActivityId",
                    "ActivityName"
            };

            financialPeriods = Arrays.asList(elm.loadFinancialPeriods(FinancialPeriod.getAllFields(), null, null).getAll());

            for (String projectId : projectIdList) {
                Project project = null;

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", projectId);
                Project[] projects = elm.loadProjects(Project.getMainFields(), projectWhere, null).getAll();
                if (projects.length == 1) project = projects[0];
                if (project == null) continue;

                P6logger.info(String.format("Processing %s", project.getName()));

                List<String> activityIdList = this.getUniqueActivityIdList(project.getId(), resourceSpreadRows);
                if (activityIdList.size() == 0) continue;

                for (String activitId : activityIdList) {

                    // Get our activity
                    Activity activity = null;
                    String activityWhere = String.format(" Id = '%s' ", activitId);
                    Activity[] activities = project.loadAllActivities(Activity.getWritableFields(), activityWhere, null).getAll();
                    if (activities.length == 1) activity = activities[0];
                    if (activity == null) continue;
                    List<ResourceSpreadRow> activityRowList = getActivityRowList(project.getId(), activity.getId(), resourceSpreadRows);
                    Date spreadStartDate = getStartDate(getActivityRowList(activity.getId(), resourceSpreadRows));
                    Date spreadFinishDate = getEndDate(getActivityRowList(activity.getId(), resourceSpreadRows));

                    //String resourceAssignmentWhere = String.format("ProjectId = '%s' and  ActivityId = '%s' ", project.getId(), activity.getId());

                    ResourceAssignment[] resourceAssignments = activity.loadResourceAssignmentsWithLiveSpread(nonSpreadFields, null, null, ResourceAssignment.getSpreadFields()
                            , SpreadPeriodType.MONTH, spreadStartDate, spreadFinishDate, false).getAll();

                    for (ResourceAssignment resourceAssignment : resourceAssignments) {
                        List<ResourceSpreadRow> activityResourceRowList = getActivityResourceRowList(activity.getId(), resourceAssignment.getResourceId(), activityRowList);
                        ResourceAssignmentSpread resourceAssignmentSpread = resourceAssignment.getResourceAssignmentSpread();
                        Iterator<SpreadPeriod> spreadIterator = resourceAssignmentSpread.getSpreadIterator(true);

                        debug = resourceAssignment.getResourceId() + " | " + resourceAssignment.getActivityId();
                        P6logger.info(debug);

                        while (spreadIterator.hasNext()) {
                            SpreadPeriod spreadPeriod = spreadIterator.next();
                            SpreadBucketType spreadBucketTypeEnum = spreadPeriod.getSpreadBucketTypeEnum();
                            String[] unitFields = resourceAssignmentSpread.getUnitFields();
                            resourceAssignmentSpread.setStartDayOfWeek(java.util.Calendar.MONDAY);
                            Date currentSpreadPeriod = spreadPeriod.getSpreadPeriodStart();
                            ResourceAssignmentSpreadPeriod resourceAssignmentSpreadPeriod = resourceAssignmentSpread.addSpreadPeriod(spreadPeriod.getSpreadPeriodStart());

                            Unit currentPlannedUnits = spreadPeriod.getUnits("PlannedUnits");
                            Unit currentRemainingUnits = spreadPeriod.getUnits("RemainingUnits");


                            ResourceSpreadRow resourcePlanningSpreadRowByDate = getResourcePlanningSpreadRowByDate(currentSpreadPeriod, activityResourceRowList);
                            if (resourcePlanningSpreadRowByDate != null) currentPlannedUnits = new Unit(resourcePlanningSpreadRowByDate.SpreadUnits);
                            resourceAssignmentSpreadPeriod.setPlannedUnits(currentPlannedUnits);

                            ResourceSpreadRow resourceRemainingSpreadRowByDate = getResourceRemainingSpreadRowByDate(currentSpreadPeriod, activityResourceRowList);
                            if (resourceRemainingSpreadRowByDate != null) currentRemainingUnits = new Unit(resourceRemainingSpreadRowByDate.SpreadUnits);
                            resourceAssignmentSpreadPeriod.setRemainingUnits(currentRemainingUnits);

                            ResourceSpreadRow resourceActualSpreadRowByDate = getResourceActualSpreadRowByDate(currentSpreadPeriod, activityResourceRowList);
                            if (resourceActualSpreadRowByDate != null) {
                                Unit currentActualUnits = new Unit(resourceActualSpreadRowByDate.SpreadUnits);
                                FinancialPeriod financialPeriod = getFinancialPeriod(resourceActualSpreadRowByDate.getSpreadPeriodDate());
                                ResourceAssignmentPeriodActual resourceAssignmentPeriodActual = new ResourceAssignmentPeriodActual(session);
                                resourceAssignmentPeriodActual.setActualUnits(currentActualUnits);
                                resourceAssignmentPeriodActual.setResourceAssignmentObjectId(resourceAssignment.getObjectId());
                                resourceAssignmentPeriodActual.setFinancialPeriodObjectId(financialPeriod.getObjectId());
                                resourceAssignmentPeriodActual.create();
                            }
                        }
                        resourceAssignment.setResourceAssignmentSpread(resourceAssignmentSpread);
                        resourceAssignment.update();
                    }
                }

            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxResourceSpread");
    }

    private List<String> getUniqueProjectIdList(List<ResourceSpreadRow> resourceSpreadRows) {
        List<String> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (retVal.contains(resourceSpreadRow.getProjectId())) continue;
            retVal.add(resourceSpreadRow.getProjectId());
        }
        return retVal;
    }

    private FinancialPeriod getFinancialPeriod(Date periodDate) {

        try {
            FinancialPeriod financialPeriod = searchForFinancialPeriod(periodDate);
            if (financialPeriod != null) return financialPeriod;

            addNewFinancialPeriod(periodDate);
            financialPeriod = searchForFinancialPeriod(periodDate);
            if (financialPeriod != null) return financialPeriod;

        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return null;
    }

    private FinancialPeriod searchForFinancialPeriod(Date periodDate){
        FinancialPeriod financialPeriod = null;
        try {

            for (FinancialPeriod thisPeriod : financialPeriods) {
                financialPeriod = thisPeriod;
                if (periodDate.getTime() >= financialPeriod.getStartDate().getTime() && periodDate.getTime() <= financialPeriod.getEndDate().getTime())
                    return thisPeriod;
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
        return financialPeriod;

    }

    private void addNewFinancialPeriod(Date periodDate) {

        try {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String financialPeriodName = formatter.format(periodDate);
            FinancialPeriod financialPeriod = new FinancialPeriod(thisSession);
            financialPeriod.setStartDate(getBeginDate(periodDate));
            financialPeriod.setEndDate(getEndDate(periodDate));
            financialPeriod.setName(financialPeriodName);
            List<FinancialPeriod> financialPeriodsToAdd = new ArrayList<>();
            financialPeriodsToAdd.add(financialPeriod);

            gob.createFinancialPeriods(financialPeriodsToAdd.toArray(new FinancialPeriod[0]));
            financialPeriods = Arrays.asList(elm.loadFinancialPeriods(FinancialPeriod.getAllFields(), null, null).getAll());
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        }
    }

    private List<String> getUniqueActivityIdList(String projectId, List<ResourceSpreadRow> resourceSpreadRows) {
        List<String> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getProjectId().equals(projectId)) continue;
            if (retVal.contains(resourceSpreadRow.getActivityId())) continue;
            retVal.add(resourceSpreadRow.getActivityId());
        }
        return retVal;
    }

    private Date getStartDate(List<ResourceSpreadRow> resourceSpreadRows) {
        Date retVal = null;
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (resourceSpreadRow.getSpreadPeriodDate() == null) continue;
            if (Utils.isLessThan(retVal, resourceSpreadRow.getSpreadPeriodDate()))
                retVal = resourceSpreadRow.getSpreadPeriodDate();
        }
        return Utils.getFirstDayOfMonth(retVal);
    }

    private Date getEndDate(List<ResourceSpreadRow> resourceSpreadRows) {
        Date retVal = null;
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (resourceSpreadRow.getSpreadPeriodDate() == null) continue;
            if (Utils.isGreaterThan(retVal, resourceSpreadRow.getSpreadPeriodDate()))
                retVal = resourceSpreadRow.getSpreadPeriodDate();
        }
        return Utils.getLastDayOfMonth(retVal);
    }

    private List<ResourceSpreadRow> getActivityRowList(String activityId, List<ResourceSpreadRow> resourceSpreadRows) {
        List<ResourceSpreadRow> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getActivityId().equals(activityId)) continue;
            retVal.add(resourceSpreadRow);
        }
        return retVal;
    }

    private List<ResourceSpreadRow> getActivityRowList(String projectId, String activityId, List<ResourceSpreadRow> resourceSpreadRows) {
        List<ResourceSpreadRow> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getProjectId().equals(projectId)) continue;
            if (!resourceSpreadRow.getActivityId().equals(activityId)) continue;
            retVal.add(resourceSpreadRow);
        }
        return retVal;
    }

    private List<ResourceSpreadRow> getActivityResourceRowList(String activityId, String resourceID, List<ResourceSpreadRow> resourceSpreadRows) {
        List<ResourceSpreadRow> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getActivityId().equals(activityId)) continue;
            if (!resourceSpreadRow.getResourceId().equals(resourceID)) continue;
            retVal.add(resourceSpreadRow);
        }
        return retVal;
    }


    /*
    private List<ResourceSpreadRow> getActivityResourceRowList(String projectId, String activityId, String resourceId,  List<ResourceSpreadRow> resourceSpreadRows){
        List<ResourceSpreadRow> retVal = new ArrayList<>();
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getProjectId().equals(projectId)) continue;
            if (!resourceSpreadRow.getActivityId().equals(activityId)) continue;
            if (!resourceSpreadRow.getResourceId().equals(resourceId)) continue;
            retVal.add(resourceSpreadRow);
        }
        return retVal;
    }
    */

    private ResourceSpreadRow getResourcePlanningSpreadRowByDate(Date periodDate, List<ResourceSpreadRow> resourceSpreadRows) {
        return getResourceSpreadRowByDate("Planned", periodDate, resourceSpreadRows);
    }

    private ResourceSpreadRow getResourceRemainingSpreadRowByDate(Date periodDate, List<ResourceSpreadRow> resourceSpreadRows) {
        return getResourceSpreadRowByDate("Remaining", periodDate, resourceSpreadRows);
    }

    private ResourceSpreadRow getResourceActualSpreadRowByDate(Date periodDate, List<ResourceSpreadRow> resourceSpreadRows) {
        return getResourceSpreadRowByDate("Actual", periodDate, resourceSpreadRows);
    }

    private ResourceSpreadRow getResourceSpreadRowByDate(String spreadType, Date periodDate, List<ResourceSpreadRow> resourceSpreadRows) {
        for (ResourceSpreadRow resourceSpreadRow : resourceSpreadRows) {
            if (!resourceSpreadRow.getSpreadUnitType().equalsIgnoreCase(spreadType)) continue;
            if (resourceSpreadRow.getSpreadPeriodDate() == null) continue;
            if (Utils.equalYearAndMonth(periodDate, resourceSpreadRow.getSpreadPeriodDate()))
                return resourceSpreadRow;
        }
        return null;
    }

    public static BeginDate getBeginDate(Date date) {
        LocalDate localDate = LocalDate.ofEpochDay(date.getTime() / (24 * 60 * 60 * 1000)).withDayOfMonth(1);
        Date firstDate = java.sql.Date.valueOf(localDate);
        return new BeginDate(firstDate.getTime());
    }

    public static EndDate getEndDate(Date date) {
        LocalDate localDate = LocalDate.ofEpochDay(date.getTime() / (24 * 60 * 60 * 1000)).plusMonths(1).withDayOfMonth(1).minusDays(1);
        Date lastDate = java.sql.Date.valueOf(localDate);
        return new EndDate(lastDate.getTime());
    }
}