package com.ggreiff.rowdata;


/**
 * Created by ggreiff on 5/25/2015.
 * ActivityExpenseRow
 */
public class ActivityExpenseRow {

    public ActivityExpenseRow() {

    }

    public ActivityExpenseRow(String projectID, String activityID, String expenseDescription, String costAccountName,
                              String expenseCategoryName, String accrualType, String documentNumber,
                              Double plannedCost, Double actualCost, Double remainingCost, Double pricePerUnit,
                              Double plannedUnits, Double actualUnits, Double remainingUnits, String unitOfMeasure) {
        ProjectId = projectID;
        ActivityId = activityID;
        ExpenseDescription = expenseDescription;
        CostAccountName = costAccountName;
        ExpenseCategoryName = expenseCategoryName;
        AccrualType = accrualType;
        DocumentNumber = documentNumber;
        PlannedCost = plannedCost;
        ActualCost = actualCost;
        RemainingCost = remainingCost;
        PricePerUnit = pricePerUnit;
        PlannedUnits = plannedUnits;
        ActualUnits = actualUnits;
        RemainingUnits = remainingUnits;
        UnitOfMeasure = unitOfMeasure;
    }

    public String ProjectId;
    public String ActivityId;
    public String ExpenseDescription;
    public String CostAccountName;
    public String ExpenseCategoryName;
    public String AccrualType;
    public String DocumentNumber;
    public Double PlannedCost;
    public Double ActualCost;
    public Double RemainingCost;
    public Double PricePerUnit;
    public Double PlannedUnits;
    public Double ActualUnits;
    public Double RemainingUnits;
    public String UnitOfMeasure;

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

    public String getExpenseDescription() {
        return ExpenseDescription;
    }

    public void setExpenseDescription(String expenseDescription) {
        ExpenseDescription = expenseDescription;
    }

    public String getCostAccountName() {
        return CostAccountName;
    }

    public void setCostAccountName(String costAccountName) {
        CostAccountName = costAccountName;
    }

    public String getExpenseCategoryName() {
        return ExpenseCategoryName;
    }

    public void setExpenseCategoryName(String expenseCategoryName) {
        ExpenseCategoryName = expenseCategoryName;
    }

    public com.primavera.integration.client.bo.enm.AccrualType getAccrualType() {

        com.primavera.integration.client.bo.enm.AccrualType retVal = com.primavera.integration.client.bo.enm.AccrualType.UNIFORM_OVER_ACTIVITY;
        if (AccrualType.equals("Start of Activity")) retVal = com.primavera.integration.client.bo.enm.AccrualType.START_OF_ACTIVITY;
        if (AccrualType.equals("End of Activity")) retVal = com.primavera.integration.client.bo.enm.AccrualType.END_OF_ACTIVITY;
        return retVal;
    }

    public void setAccrualType(String accrualType) {
        AccrualType = accrualType;
    }

    public String getDocumentNumber() {
        return DocumentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        DocumentNumber = documentNumber;
    }

    public Double getPlannedCost() {
        return PlannedCost;
    }

    public void setPlannedCost(Double plannedCost) {
        PlannedCost = plannedCost;
    }

    public Double getActualCost() {
        return ActualCost;
    }

    public void setActualCost(Double actualCost) {
        ActualCost = actualCost;
    }

    public Double getRemainingCost() {
        return RemainingCost;
    }

    public void setRemainingCost(Double remainingCost) {
        RemainingCost = remainingCost;
    }

    public Double getPricePerUnit() {
        return PricePerUnit;
    }

    public void setPricePerUnit(Double pricePerUnit) {
        PricePerUnit = pricePerUnit;
    }

    public Double getPlannedUnits() {
        return PlannedUnits;
    }

    public void setPlannedUnits(Double plannedUnits) {
        PlannedUnits = plannedUnits;
    }

    public Double getActualUnits() {
        return ActualUnits;
    }

    public void setActualUnits(Double actualUnits) {
        ActualUnits = actualUnits;
    }

    public Double getRemainingUnits() {
        return RemainingUnits;
    }

    public void setRemainingUnits(Double remainingUnits) {
        RemainingUnits = remainingUnits;
    }

    public String getUnitOfMeasure() {
        return UnitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        UnitOfMeasure = unitOfMeasure;
    }

}
