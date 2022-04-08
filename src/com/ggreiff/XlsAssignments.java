
package com.ggreiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.primavera.common.value.ObjectId;
import com.primavera.common.value.Unit;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.RMIURL;
import com.primavera.integration.client.Session;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.Resource;
import com.primavera.integration.client.bo.object.ResourceAssignment;
import com.primavera.integration.common.DatabaseInstance;

/**
 * Created by ggreiff on 1/23/2015.
 */
public class XlsAssignments {

    public static void main(String[] args) {

        Session session = null;
        String host = "Bavaria";
        int port = 9099;
        String user = "admin";
        String password = "password";
        String databaseName = "P6Demo";
        String databaseId = "";
        String xlsFileName = "Assignments.xlsx";

        try {
        	DatabaseInstance[] dbInstances = Session.getDatabaseInstances(RMIURL.getRmiUrl( RMIURL.LOCAL_SERVICE ));  //.getDatabaseInstances(RMIURL.LOCAL_SERVICE);
            for (DatabaseInstance dbInstance : dbInstances) {
                if (dbInstance.getDatabaseName().equals(databaseName)) {
                    databaseId = dbInstance.getDatabaseId();
                    break;
                }
            }

            session = Session.login(RMIURL.getRmiUrl(RMIURL.STANDARD_RMI_SERVICE, host, port), databaseId, user, password);
            EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

            XlsHelper xlsHelper = new XlsHelper(xlsFileName);
            List<Assignment> assignments = xlsHelper.GetAssignments();


            for (Assignment assignment : assignments) {

                // vars we are going to need
                Project project = null;
                Activity activity = null;
                Resource resource = null;
                ResourceAssignment resourceAssignment = null;

                // Get our project
                String projectWhere = String.format(" Id = '%s' ", assignment.getProjectID());
                BOIterator<Project> projectBOIterator = elm.loadProjects(new String[]{"Id", "Status", "Name", "StartDate", "FinishDate"}, projectWhere, null);
                while (projectBOIterator.hasNext()) { // Should only be one by ID
                    project = projectBOIterator.next();
                }
                if (project == null) continue;

                // Get our activity
                String activityWhere = String.format(" Id = '%s' ", assignment.getActivityID());
                BOIterator<Activity> activityBOIterator = project.loadAllActivities(new String[]{"Id", "Status", "Name", "StartDate", "FinishDate"}, activityWhere, null);
                while (activityBOIterator.hasNext()) { // Should only be one by ID
                    activity = activityBOIterator.next();
                }
                if (activity == null) continue;

                // Get our Resource
                String resourceWhere = String.format(" Id = '%s' ", assignment.getResourceID());
                BOIterator<Resource> resourceBOIterator = elm.loadResources(new String[]{"Id", "Name"}, resourceWhere, null);
                while (resourceBOIterator.hasNext()) {
                    resource = resourceBOIterator.next();
                }
                if (resource == null) continue;

                Boolean foundAssignment = false;
                BOIterator<ResourceAssignment> resourceAssignmentBOIterator = activity.loadResourceAssignments(new String[]{"ResourceId", "ResourceName"}, null, null);
                while (resourceAssignmentBOIterator.hasNext()) {
                    resourceAssignment = resourceAssignmentBOIterator.next();
                    if (resourceAssignment.getResourceId().equals(resource.getId())) {
                        String message = String.format("%s is already assigned to %s on project %s", resource.getName(), activity.getName(), project.getName());
                        System.out.println(message);
                        foundAssignment = true;
                       
                        Unit unit = new Unit(assignment.UnitDouble);
                        resourceAssignment.setPlannedUnits(unit);
                        resourceAssignment.update();
                        
                       
                        break;
                    }
                }
                    if (foundAssignment) continue;
                    List<ResourceAssignment> resourceAssignments = new ArrayList<>(Arrays.asList(resourceAssignmentBOIterator.getAll()));
                    ResourceAssignment newAssignment = new ResourceAssignment(session);
                    newAssignment.setActivityObjectId(activity.getObjectId());
                    newAssignment.setResourceObjectId(resource.getObjectId());
                    ObjectId objectId = newAssignment.create();
                    newAssignment.setObjectId(objectId);
                    resourceAssignments.add(newAssignment);
                    activity.updateResourceAssignments(resourceAssignments.toArray(new ResourceAssignment[0]));
                    String message = String.format("%s is now assigned to %s on project %s", resource.getName(), activity.getName(), project.getName());
                    System.out.println(message);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (session != null) session.logout();
        }
    }
}