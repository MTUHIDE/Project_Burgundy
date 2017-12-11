package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Databases.AppointmentsDB;
import controllers.Databases.AvailabilityDB;
import models.AppointmentsModel;
import models.AvailabilityModel;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AvailabilityController extends Controller {

    public Result createAvailability() {
   /* Get user object from request */
        JsonNode json = request().body().asJson();
        /* Get user from json request */
        AvailabilityModel availability = new AvailabilityModel();
        availability.setavailabilityId(json.findPath("availabilityId").textValue());
        availability.setUserid(json.findPath("userId").textValue());
        availability.setStartDate(DatatypeConverter.parseDateTime(json.findPath("startDate").textValue()).getTime());
        availability.setEndDate(DatatypeConverter.parseDateTime(json.findPath("endDate").textValue()).getTime());
        availability.setWeekly(json.findPath("weekly").booleanValue());
        /* Check if user is in DB */
        AvailabilityDB.addAvailability(availability);
        return ok();
    }

    public Result availableSlots(String userId) {
        List<AvailabilityModel> availabilities = AvailabilityDB.getAvailabilitesForUser(userId);
        return ok(Json.toJson(availabilities));
    }

    public Result availableSlotsForAppointments(String userId) {
        List<AvailabilityModel> availabilities = AvailabilityDB.getAvailabilitesForUser(userId);
        List<AppointmentsModel> appointments = AppointmentsDB.getAppointmentsForUser("Coach", userId);
        for (int j = 0; j < availabilities.size(); j++) {
            AvailabilityModel a = availabilities.get(j);
            Date startDate = new DateTime(a.getStartDate()).toDate();
            Date endDate = new DateTime( a.getEndDate() ).toDate();
            long duration = endDate.getTime() - startDate.getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            if (diffInMinutes / 30 != 1) {
                for (int i = 0; i < diffInMinutes / 30; i++) {
                    availabilities.add(new AvailabilityModel
                            (a.getavailabilityId(),
                                    userId,
                                    DateUtils.addMinutes(a.getStartDate(), 30 * i),
                                    DateUtils.addMinutes(a.getStartDate(), 30 * (i + 1)),
                                    a.getWeekly()
                            ));
                }
                availabilities.remove(a);
                j--;
            }
        }
        ArrayList<AvailabilityModel> toRemove = new ArrayList<AvailabilityModel>();
        for(AvailabilityModel av : availabilities) {
            for(AppointmentsModel ap : appointments) {
                Date availabilityStart = new DateTime( av.getStartDate() ).toDate();
                Date availabilityEnd = new DateTime( av.getEndDate() ).toDate();
                Date appointmentStart = new DateTime( ap.getStartDate() ).toDate();
                Date appointmentEnd = new DateTime( ap.getEndDate() ).toDate();
                if( (appointmentStart.before(availabilityStart) || appointmentStart.equals(availabilityStart)) && (appointmentEnd.after(availabilityEnd) || appointmentEnd.equals(availabilityEnd)) ) {
                    toRemove.add(av);
                }
            }
        }
        availabilities.removeAll(toRemove);
        return ok(Json.toJson(availabilities));
    }

    public Result removeAvailability() {
        JsonNode json = request().body().asJson();
        String availabilityId = json.findPath("availabilityId").asText();
        AvailabilityDB.removeAvailability(availabilityId);
        return ok();
    }
}
