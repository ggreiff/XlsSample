package com.ggreiff.p6;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.ggreiff.CommandArgs;
import com.ggreiff.helpers.XlsxActivityCodeHelper;
import com.ggreiff.helpers.XlsxAssignmentHelper;
import com.ggreiff.rowdata.AssignmentRow;
import com.primavera.integration.client.Session;
import com.primavera.common.value.Unit;
import com.primavera.common.value.spread.ResourceAssignmentSpread;
import com.primavera.common.value.spread.ResourceAssignmentSpreadPeriod;
import com.primavera.common.value.spread.SpreadPeriod;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.enm.SpreadPeriodType;
import com.primavera.integration.client.bo.object.Activity;
import com.primavera.integration.client.bo.object.FinancialPeriod;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.Resource;
import com.primavera.integration.client.bo.object.ResourceAssignment;
import com.primavera.integration.client.bo.object.ResourceAssignmentPeriodActual;
import com.primavera.integration.util.WhereClauseHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by ggreiff on 1/23/2015.
 *XlsAssignments
 */
public class XlsxAssignments {

	final static Logger P6logger =  LogManager.getLogger(XlsxAssignments.class);

	public void run(Session session, CommandArgs commandArgs) {
		P6logger.info("Starting XlsxActivityExpenses");
		try {

			EnterpriseLoadManager elm = session.getEnterpriseLoadManager();

			XlsxAssignmentHelper xlsxAssignmentHelper = new XlsxAssignmentHelper(commandArgs);
			List<AssignmentRow> assignmentRows = xlsxAssignmentHelper.getAssignmentsRows();
			List<String> UniqueAssignments = new ArrayList<>();

			Date earlyStart = new Date(Long.MAX_VALUE);
			Date latestFinish = new Date(Long.MIN_VALUE);
			for (AssignmentRow assignment : assignmentRows) {
				if (assignment.PeriodDate.before(earlyStart))
					earlyStart = assignment.PeriodDate;
				if (assignment.PeriodDate.after(latestFinish))
					latestFinish = assignment.PeriodDate;
				if (UniqueAssignments.contains(assignment.getKey()))
					continue;
				UniqueAssignments.add(assignment.getKey());
			}

			String dateBegin = WhereClauseHelper.formatDate(session, earlyStart);
			String dateEnd = WhereClauseHelper.formatDate(session, latestFinish);
			String whereClause = "StartDate >= " + dateBegin + " and StartDate <= " + dateEnd;

			BOIterator<FinancialPeriod> financialPeriodBoi = elm.loadFinancialPeriods(FinancialPeriod.getAllFields(), whereClause, null);
			FinancialPeriod[] financialPeriods = financialPeriodBoi.getAll();

			// Loop through our assignments that were read from the spreadsheet
			for (String uniqueAssignments : UniqueAssignments) {

				AssignmentRow assignmentRow = AssignmentRow.getKeyIds(uniqueAssignments);
				if (assignmentRow == null) continue;

				// variables we are going to need
				Project project = null;
				Activity activity = null;
				Resource resource = null;
				ResourceAssignment resourceAssignment = null;

				// Get our project

				String projectWhere = String.format(" Id = '%s' ", assignmentRow.getProjectID());
				BOIterator<Project> projectBoi = elm.loadProjects(Project.getMainFields(), projectWhere, null);
				while (projectBoi.hasNext()) { // Should only be one by
					project = projectBoi.next();
				}
				if (project == null)
					continue;

				// Get our activity
				String activityWhere = String.format(" Id = '%s' ", assignmentRow.getActivityID());
				BOIterator<Activity> activityBoi = project.loadAllActivities(Activity.getWritableFields(), activityWhere, null);
				while (activityBoi.hasNext()) { // Should only be one by ID
					activity = activityBoi.next();
				}
				if (activity == null)
					continue;

				// Get our Resource
				String resourceWhere = String.format(" Id = '%s' ", assignmentRow.getResourceID());
				BOIterator<Resource> resourceBoi = elm.loadResources(Resource.getMainFields(), resourceWhere, null);
				while (resourceBoi.hasNext()) {
					resource = resourceBoi.next();
				}
				if (resource == null) {
					continue;
				}

				// Look for the resource assignment
				Boolean foundAssignment = false;
				BOIterator<ResourceAssignment> resourceAssignmentBoi = activity.loadResourceAssignments(ResourceAssignment.getMainFields(), null, null);
				while (resourceAssignmentBoi.hasNext()) {
					resourceAssignment = resourceAssignmentBoi.next();
					if (resourceAssignment.getResourceId().equals(resource.getId())) {
						foundAssignment = true;
						break;
					}
				}
				if (!foundAssignment)
					continue;

				//
				// ResourceAssignmentSpread
				//
				String[] unitFields = new String[]{"RemainingUnits", "PlannedUnits"};
				resourceAssignment = ResourceAssignment.loadWithLiveSpread(session, new String[]{"PlannedUnitsPerTime", "ResourceId", "ActivityId", "PlannedUnits", "ResourceName"}, resourceAssignment.getObjectId(), unitFields, SpreadPeriodType.MONTH,
						activity.getStartDate(), activity.getFinishDate(), false);
				ResourceAssignmentSpread resourceAssignmentSpread = resourceAssignment.getResourceAssignmentSpread();

				//
				// ResourceAssignmentPeriodActual
				//
				String resourceAssignmentPeriodActualWhere = String.format(" ResourceAssignmentObjectId = '%s' ", resourceAssignment.getObjectId());
				BOIterator<ResourceAssignmentPeriodActual> resourceAssignmentPeriodActualBoi = resourceAssignment.loadResourceAssignmentPeriodActuals(new String[]{"FinancialPeriodObjectId", "ResourceAssignmentObjectId", "ActualUnits",
						"ActualCost"}, resourceAssignmentPeriodActualWhere, null);

				// let the user know what we found
				String message = String.format("%s is assigned to %s on project %s", resource.getName(), activity.getName(), project.getName());
				P6logger.info(message);

				//
				// Process spreads found in the spreadsheet
				//
				Boolean updateResourceSpread = false;
				Iterator<SpreadPeriod> spreadPeriodIterator = resourceAssignmentSpread.getSpreadIterator(true);
				while (spreadPeriodIterator.hasNext()) {
					SpreadPeriod spreadPeriod = spreadPeriodIterator.next();
					for (String unitType : unitFields) {
						for (AssignmentRow assignmentDetails : assignmentRows) {
							if (!assignmentDetails.getKey().equals(assignmentRow.getKey()))
								continue;
							if (!(assignmentDetails.UnitType.equals(unitType)))
								continue;
							if (assignmentDetails.getPeriodDate().compareTo(spreadPeriod.getSpreadPeriodStart()) >= 0 && assignmentDetails.getPeriodDate().compareTo(spreadPeriod.getSpreadPeriodEnd()) < 0) {
								ResourceAssignmentSpreadPeriod resourceAssignmentSpreadPeriod = resourceAssignmentSpread.addSpreadPeriod(spreadPeriod.getSpreadPeriodStart());
								Unit unit = spreadPeriod.getUnits(assignmentDetails.getUnitType());
								unit.setDoubleValue(assignmentDetails.getDoublePlannedUnits());
								if (unitType.equals("RemainingUnits"))
									resourceAssignmentSpreadPeriod.setRemainingUnits(unit);
								if (unitType.equals("PlannedUnits"))
									resourceAssignmentSpreadPeriod.setPlannedUnits(unit);
								updateResourceSpread = true;
								String spreadMessage = String.format("SpreadbucketType = %1$s from %2$tF to %3$tF for %4$f %5$s units", spreadPeriod.getSpreadBucketTypeEnum().toString(), spreadPeriod.getSpreadPeriodStart(),
										spreadPeriod.getSpreadPeriodEnd(), spreadPeriod.getUnits(unitType).doubleValue(), unitType);
								P6logger.info(spreadMessage);
							}
						}
					}
				}
				//
				// Spreads appear to be always over written
				//
				if (updateResourceSpread) {
					resourceAssignment.setResourceAssignmentSpread(resourceAssignmentSpread);
					resourceAssignment.update();
				}

				//
				// Process actuals found in the spreadsheet
				//
				for (AssignmentRow assignmentDetails : assignmentRows) {
					if (!(assignmentDetails.UnitType.equals("ActualUnits")))
						continue;
					for (FinancialPeriod financialPeriod : financialPeriods) {
						if (!(assignmentDetails.PeriodDate.compareTo(financialPeriod.getStartDate()) >= 0 && assignmentDetails.PeriodDate.compareTo(financialPeriod.getEndDate()) <= 0))
							continue;
						try {
							Boolean foundresourceAssignmentPeriodActual = false;
							while (resourceAssignmentPeriodActualBoi.hasNext()) {
								ResourceAssignmentPeriodActual resourceAssignmentPeriodActual = resourceAssignmentPeriodActualBoi.next();
								if (!resourceAssignmentPeriodActual.getObjectId().equals(financialPeriod.getObjectId()))
									continue;
								resourceAssignmentPeriodActual.setActualUnits(new Unit(assignmentDetails.UnitDouble));
								resourceAssignmentPeriodActual.update();
								foundresourceAssignmentPeriodActual = true;
								break;
							}

							if (!foundresourceAssignmentPeriodActual) {
								ResourceAssignmentPeriodActual resourceAssignmentPeriodActual = new ResourceAssignmentPeriodActual(session);
								resourceAssignmentPeriodActual.setActualUnits(new Unit(assignmentDetails.UnitDouble));
								resourceAssignmentPeriodActual.setFinancialPeriodObjectId(financialPeriod.getObjectId());
								resourceAssignmentPeriodActual.setResourceAssignmentObjectId(resourceAssignment.getObjectId());
								resourceAssignmentPeriodActual.create();
							}
						} catch (Exception ex) {
							P6logger.warn(ex.getMessage());
						}
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
}