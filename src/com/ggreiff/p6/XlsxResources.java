package com.ggreiff.p6;

import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxResourceHelper;
import com.ggreiff.rowdata.ResourceRow;
import com.primavera.integration.client.bo.enm.ResourceType;
import com.primavera.common.value.ObjectId;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.object.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ggreiff on 5/25/2015.
 * XlsResources
 */
public class XlsxResources {


    final static Logger P6logger = Logger.getLogger(XlsxResources.class);

    public void run(Session session, CommandArgs commandArgs) {

        P6logger.info("Starting XlsxResource");
        try {

            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

            XlsxResourceHelper xlsxResourceHelper = new XlsxResourceHelper(commandArgs);
            List<ResourceRow> resourceRows = xlsxResourceHelper.getResourceRows();

            //
            // Get a map of calendars
            //
            Map<String,Calendar> calendarMap = new HashMap<>();
            Calendar[] calendars = elm.loadCalendars(Calendar.getAllFields(), null, null).getAll();
            for (Calendar calendar :  calendars){
                if (calendarMap.containsKey(calendar.getName())) continue;
                calendarMap.put(calendar.getName(), calendar);
            }

            for (ResourceRow resourceRow : resourceRows) {
                try {
                    if (resourceRow.getResourceType().equals(ResourceType.NULL)) {
                        P6logger.info(String.format("A resource must have a type %s", ResourceType.NULL));
                        continue;
                    }

                    Resource resource = null;
                    String resourceWhere = String.format(" Id = '%s' ", resourceRow.getResourceId());
                    Resource[] resources = elm.loadResources(Resource.getWritableFields(), resourceWhere, null).getAll();
                    if (resources.length == 1) resource = resources[0];
                    if (resource != null) {
                        P6logger.info(String.format("Found existing %s", resource.getName()));
                        continue;
                    }

                    P6logger.info(String.format("Processing %s", resourceRow.getResourceName()));

                    resource = new Resource(session);
                    resource.setId(resourceRow.getResourceId());
                    resource.setName(resourceRow.getResourceName());
                    resource.setResourceType(resourceRow.getResourceType());
                    if (calendarMap.containsKey(resourceRow.getCalendar())) {
                        Calendar calendar = calendarMap.get(resourceRow.getCalendar());
                        resource.setCalendarObjectId(calendar.getObjectId());
                    }
                    ResourceRate resourceRate = new ResourceRate(session);
                    resourceRate.setEffectiveDate(resourceRow.getEffectiveDate());
                    resourceRate.setMaxUnitsPerTime(resourceRow.getMaxUnits());
                    resourceRate.setPricePerUnit(resourceRow.getPriceUnit());
                    resourceRate.setPricePerUnit2(resourceRow.getPriceUnit2());
                    resourceRate.setPricePerUnit3(resourceRow.getPriceUnit3());
                    resourceRate.setPricePerUnit4(resourceRow.getPriceUnit4());
                    resourceRate.setPricePerUnit5(resourceRow.getPriceUnit5());

                    ObjectId resoureObjectId = resource.create();
                    resource.setObjectId(resoureObjectId);
                    resource.update();

                    resourceRate.setResourceObjectId(resoureObjectId);
                    ObjectId resourceRateObjectId = resourceRate.create();
                    resourceRate.setObjectId(resourceRateObjectId);
                    resourceRate.update();
                }
                catch (Exception ex){
                    P6logger.warn(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            P6logger.error(ex.getMessage());
        } finally {
            if (session != null)
                session.logout();
        }
        P6logger.info("Finished XlsxResource");
    }
}
