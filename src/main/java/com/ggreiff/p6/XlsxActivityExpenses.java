package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxActivityExpenseHelper;
import com.ggreiff.rowdata.ActivityExpenseRow;
import com.primavera.common.value.Cost;
import com.primavera.common.value.ObjectId;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.object.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggreiff on 5/25/2015.
 */
public class XlsxActivityExpenses {


    final static Logger P6logger =  LogManager.getLogger(XlsxActivityExpenses.class);

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxActivityExpenses");
        try {

            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

            XlsxActivityExpenseHelper xlsxActivityExpenseHelper = new XlsxActivityExpenseHelper(commandArgs);
            List<ActivityExpenseRow> activityExpenseRows = xlsxActivityExpenseHelper.getActivityExpenseRows();
            List<String> projectIdList = this.getUniqueProjectIdList(activityExpenseRows);

            //
            // Get a map of cost account
            //
            Map<String,CostAccount> costAccountMap = new HashMap<>();
            CostAccount[] costAccounts = elm.loadCostAccounts(CostAccount.getAllFields(), null, null).getAll();
            for (CostAccount costAccount :  costAccounts){
                if (costAccountMap.containsKey(costAccount.getName())) continue;
                costAccountMap.put(costAccount.getName(), costAccount);
            }

            //
            // Get a map of expense categories
            //
            Map<String, ExpenseCategory> expenseCategoryMap = new HashMap<>();
            ExpenseCategory[] expenseCategories = elm.loadExpenseCategories(ExpenseCategory.getAllFields(),null,null).getAll();
            for(ExpenseCategory expenseCategory : expenseCategories) {
                if (expenseCategoryMap.containsKey(expenseCategory.getName())) continue;
                expenseCategoryMap.put(expenseCategory.getName(),expenseCategory);
            }


            for (String projectId : projectIdList) {
                Project project = null;

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", projectId);
                Project[] projects = elm.loadProjects(Project.getMainFields(), projectWhere, null).getAll();
                if (projects.length == 1) project = projects[0];
                if (project == null) continue;

                P6logger.info(String.format("Processing %s", project.getName()));

                List<String> activityIdList = this.getUniqueActivityIdList(project.getId(),activityExpenseRows);
                if(activityIdList.size()  == 0) continue;

                for (String activitId : activityIdList) {

                    // Get our activity
                    Activity activity = null;
                    String activityWhere = String.format(" Id = '%s' ", activitId);
                    Activity[] activities = project.loadAllActivities(Activity.getWritableFields(), activityWhere, null).getAll();
                    if (activities.length == 1) activity = activities[0];
                    if (activity == null) continue;

                    //
                    // Get a list of expenses for this activity
                    //
                    List<ActivityExpenseRow> activityExpenseRowList = this.getActivityExpenseRowList(project.getId(), activity.getId(), activityExpenseRows);
                    for(ActivityExpenseRow activityExpenseRow : activityExpenseRowList){

                        ActivityExpense activityExpense = new ActivityExpense(session);
                        activityExpense.setActivityObjectId(activity.getObjectId());
                        activityExpense.setExpenseItem(activityExpenseRow.ExpenseDescription);
                        activityExpense.setExpenseDescription(activityExpenseRow.ExpenseDescription);
                        activityExpense.setDocumentNumber(activityExpenseRow.DocumentNumber);
                        activityExpense.setUnitOfMeasure(activityExpenseRow.getUnitOfMeasure());
                        activityExpense.setAccrualType(activityExpenseRow.getAccrualType());
                        activityExpense.setPlannedCost(new Cost(activityExpenseRow.getPlannedCost()));
                        activityExpense.setActualCost(new Cost(activityExpenseRow.getActualCost()));
                        activityExpense.setRemainingCost(new Cost(activityExpenseRow.getRemainingCost()));
                        activityExpense.setPlannedUnits(activityExpenseRow.getPlannedUnits());
                        activityExpense.setActualUnits(activityExpenseRow.getActualUnits());
                        activityExpense.setRemainingUnits(activityExpenseRow.getRemainingUnits());
                        if (costAccountMap.containsKey(activityExpenseRow.getCostAccountName()))
                            activityExpense.setCostAccountObjectId(costAccountMap.get(activityExpenseRow.getCostAccountName()).getObjectId());
                        if (expenseCategoryMap.containsKey(activityExpenseRow.getExpenseCategoryName()))
                            activityExpense.setExpenseCategoryObjectId(expenseCategoryMap.get(activityExpenseRow.getExpenseCategoryName()).getObjectId());
                        ObjectId objectId = activityExpense.create();
                        activityExpense.setObjectId(objectId);
                        activityExpense.update();
                    }
                }

            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxActivityExpenses");
    }

    private List<String> getUniqueProjectIdList(List<ActivityExpenseRow> activityExpenseRows) {
        List<String> retVal = new ArrayList<>();
        for (ActivityExpenseRow activityExpenseRow : activityExpenseRows) {
            if (retVal.contains(activityExpenseRow.getProjectId())) continue;
            retVal.add(activityExpenseRow.getProjectId());
        }
        return retVal;
    }

    private List<String> getUniqueActivityIdList(String projectId, List<ActivityExpenseRow> activityExpenseRows) {
        List<String> retVal = new ArrayList<>();
        for (ActivityExpenseRow activityExpenseRow : activityExpenseRows) {
            if (!activityExpenseRow.getProjectId().equals(projectId)) continue;
            if (retVal.contains(activityExpenseRow.getActivityId())) continue;
            retVal.add(activityExpenseRow.getActivityId());
        }
        return retVal;
    }

    private List<ActivityExpenseRow> getActivityExpenseRowList(String projectId,String activityId, List<ActivityExpenseRow> activityExpenseRows){
        List<ActivityExpenseRow> retVal = new ArrayList<>();
        for (ActivityExpenseRow activityExpenseRow : activityExpenseRows) {
            if (!(activityExpenseRow.getProjectId().equals(projectId) && activityExpenseRow.getActivityId().equals(activityId))) continue;
            retVal.add(activityExpenseRow);
        }
        return retVal;
    }

}
