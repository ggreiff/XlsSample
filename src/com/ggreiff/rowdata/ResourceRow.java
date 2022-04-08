package com.ggreiff.rowdata;



import com.primavera.common.value.BeginDate;
import com.primavera.common.value.Cost;
import com.primavera.common.value.UnitsPerTime;
import com.primavera.integration.client.bo.enm.ResourceType;

import java.util.Date;

/**
 * Created by ggreiff on 5/25/2015.
 * ResourceRow
 *
 */
public class ResourceRow {

    public ResourceRow( String resourceId, String resourceName, String resourceType,
                       String calendar, Date effectiveDate, Double maxUnits,
                       Double priceUnit, Double priceUnit2, Double priceUnit3, Double priceUnit4, Double priceUnit5) {
       ResourceId = resourceId;
        ResourceName = resourceName;
        ResourceTypeValue = resourceType;
        Calendar = calendar;
        EffectiveDate = effectiveDate;
        MaxUnits = maxUnits;
        PriceUnit = priceUnit;
        PriceUnit2 = priceUnit2;
        PriceUnit3 = priceUnit3;
        PriceUnit4 = priceUnit4;
        PriceUnit5 = priceUnit5;
    }

    public String ResourceId;
    public String ResourceName;
    public String ResourceTypeValue;
    public String Calendar;
    public Date EffectiveDate;
    public Double MaxUnits;
    public Double PriceUnit;
    public Double PriceUnit2;
    public Double PriceUnit3;
    public Double PriceUnit4;
    public Double PriceUnit5;

    //The resource type: "Labor", "Nonlabor", or "Material".

    public String getResourceId() {
        return ResourceId;
    }

    public void setResourceId(String resourceId) { ResourceId = resourceId; }

    public String getResourceName() {
        return ResourceName;
    }

    public void setResourceName(String resourceName) {
        ResourceName = resourceName;
    }

    public ResourceType getResourceType() {

        if (ResourceTypeValue.equalsIgnoreCase("labor")) return ResourceType.LABOR;
        if (ResourceTypeValue.equalsIgnoreCase("nonlabor")) return ResourceType.NONLABOR;
        if (ResourceTypeValue.equalsIgnoreCase("material")) return ResourceType.MATERIAL;
        return ResourceType.NULL;
    }

    public void setResourceType(String resourceType) {
        ResourceTypeValue = resourceType;
    }

    public String getCalendar() {
        return Calendar;
    }

    public void setCalendar(String calendar) {
        Calendar = calendar;
    }

    public BeginDate getEffectiveDate() { return new BeginDate(EffectiveDate); }

    public void setEffectiveDate(Date effectiveDate) {
        EffectiveDate = effectiveDate;
    }

    public UnitsPerTime getMaxUnits() {
        return new UnitsPerTime(MaxUnits);
    }

    public void setMaxUnits(Double maxUnits) {
        MaxUnits = maxUnits;
    }

    public Cost getPriceUnit() {
        return GetCost(PriceUnit);
    }

    public void setPriceUnit(Double priceUnit) {
        PriceUnit = priceUnit;
    }

    public Cost getPriceUnit2() {
        return GetCost(PriceUnit2);
    }

    public void setPriceUnit2(Double priceUnit2) {
        PriceUnit2 = priceUnit2;
    }

    public Cost getPriceUnit3() {
        return GetCost(PriceUnit3);
    }

    public void setPriceUnit3(Double priceUnit3) {
        PriceUnit3 = priceUnit3;
    }

    public Cost getPriceUnit4() {
        return GetCost(PriceUnit4);
    }

    public void setPriceUnit4(Double priceUnit4) {
        PriceUnit4 = priceUnit4;
    }

    public Cost getPriceUnit5() {
        return GetCost(PriceUnit5);
    }

    public void setPriceUnit5(Double priceUnit5) {
        PriceUnit5 = priceUnit5;
    }

    private Cost GetCost(Double cost){
        return new Cost(cost);
    }

}
