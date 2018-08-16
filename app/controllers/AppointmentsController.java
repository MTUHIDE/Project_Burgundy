package controllers;

import application_components.annotations.Authenticate;
import com.fasterxml.jackson.databind.JsonNode;
import application_components.mailing.MailerService;
import databases.AppointmentsDB;
import databases.SettingsDB;
import databases.UserDB;
import models.AppointmentsModel;
import models.AvailabilityModel;
import models.UsersModel;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.xml.bind.DatatypeConverter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AppointmentsController extends Controller {
    private AppointmentsDB appointmentsDB = new AppointmentsDB();
    private SettingsDB settingsDB = new SettingsDB();
    private MailerService mailerService = new MailerService();
    private UserDB userDB = new UserDB();

    @Authenticate
    public Result index() {
        return ok(views.html.pages.appointments.render());
    }

    @Authenticate(role = "Coach")
    public Result updatePresence() {
        JsonNode json = request().body().asJson();
        AppointmentsModel appointment = appointmentsDB.get(json.findPath("appointmentId").textValue()).orElseThrow(NullPointerException::new);
        appointment.setPresent(json.findPath("present").asBoolean());
        appointmentsDB.addOrUpdate(appointment);
        return ok();
    }

    @Authenticate
    public Result createAppointment() {
        JsonNode json = request().body().asJson();
        AppointmentsModel availability = appointmentsDB.get(json.findPath("appointmentId").asText()).get();
        Date startDate = DatatypeConverter.parseDateTime(json.findPath("startDate").textValue()).getTime();
        Date endDate = DatatypeConverter.parseDateTime(json.findPath("endDate").textValue()).getTime();
        UsersModel student = userDB.get(json.findPath("studentId").asText()).get();
        AppointmentsModel appointment = availability.clone();
        appointment.setDescription(null);
        appointment.setPresent(false);
        appointment.setStartDate(startDate);
        appointment.setEndDate(endDate);
        if ( !json.findPath("weekly").asBoolean() ) {
            appointment.setWeeklyId(null);
            appointment.setWeekly(false);
        }
        if ( availability.isWeekly() ) {
            String UUID1 = UUID.randomUUID().toString();
            String UUID2 = UUID.randomUUID().toString();
            Calendar startAppointment = DatatypeConverter.parseDateTime(json.findPath("startDate").textValue());
            Calendar endAppointment = DatatypeConverter.parseDateTime(json.findPath("endDate").textValue());
            List<AppointmentsModel> weeklyAvailabilities = appointmentsDB.getByWeeklyId(availability.getWeeklyId());
            Calendar startWeekly = Calendar.getInstance();
            startWeekly.setTime(weeklyAvailabilities.get(0).getStartDate());
            while ( startWeekly.get(Calendar.WEEK_OF_YEAR) != startAppointment.get(Calendar.WEEK_OF_YEAR) ) {
                startAppointment.add(Calendar.DAY_OF_YEAR, -7);
                endAppointment.add(Calendar.DAY_OF_YEAR, -7);
            }
            for ( AppointmentsModel weeklyAvailability : weeklyAvailabilities ) {
                appointment.setStartDate(startAppointment.getTime());
                appointment.setEndDate(endAppointment.getTime());
                if ( appointment.isWeekly() ) {
                    if ( appointment.getStartDate().after(startDate) || appointment.getStartDate().equals(startDate)) {
                        split(weeklyAvailability, student, appointment, UUID1, UUID2, true);
                    } else {
                        split(weeklyAvailability, student, appointment, UUID1, UUID2, false);
                    }
                } else {
                    System.out.println(appointment.getStartDate() + " " + startDate);
                    if ( appointment.getStartDate().equals(startDate) ) {
                        System.out.println("A");
                        split(weeklyAvailability, student, appointment, UUID1, UUID2, true);
                    } else {
                        split(weeklyAvailability, student, appointment, UUID1, UUID2, false);
                    }
                }
                startAppointment.add(Calendar.DAY_OF_YEAR, 7);
                endAppointment.add(Calendar. DAY_OF_YEAR, 7);
            }
        } else {
            split(availability, student, appointment);
        }

        return ok();
    }

    private AppointmentsModel split(AppointmentsModel availability, UsersModel student, AppointmentsModel appointment) {
        if (availability.getStartDate().before(appointment.getStartDate()) && availability.getEndDate().equals(appointment.getEndDate())) {
            availability.setEndDate(appointment.getStartDate());
            appointment.setAppointmentId(null);
            appointmentsDB.addOrUpdate(availability);
        } else if (availability.getStartDate().equals(appointment.getStartDate()) && availability.getEndDate().after(appointment.getEndDate())) {
            availability.setStartDate(appointment.getEndDate());
            appointment.setAppointmentId(null);
            appointmentsDB.addOrUpdate(availability);
        } else if (availability.getStartDate().before(appointment.getStartDate()) && availability.getEndDate().after(appointment.getEndDate())) {
            AppointmentsModel newAvailability = availability.clone();
            newAvailability.setStartDate(appointment.getEndDate());
            newAvailability.setEndDate(availability.getEndDate());
            newAvailability.setAppointmentId(null);
            availability.setEndDate(appointment.getStartDate());
            appointment.setAppointmentId(null);
            appointmentsDB.addOrUpdate(availability);
            appointmentsDB.addOrUpdate(newAvailability);
        }
        appointment.setStudentData(student.getUid(), student.getEmail(), student.getDisplayName(), student.getPhotoURL());
        appointmentsDB.addOrUpdate(appointment);
        return appointment;
    }

    private AppointmentsModel split(AppointmentsModel availability, UsersModel student, AppointmentsModel appointment, String UUID1, String UUID2, boolean isAppointment) {
        System.out.println(isAppointment);
        if (availability.getStartDate().before(appointment.getStartDate()) && availability.getEndDate().equals(appointment.getEndDate())) {
            availability.setEndDate(appointment.getStartDate());
            appointment.setAppointmentId(null);
            if ( appointment.isWeekly() || (!appointment.isWeekly() && !isAppointment) ) {
                appointment.setWeeklyId(UUID1);
            }
            appointmentsDB.addOrUpdate(availability);
        } else if (availability.getStartDate().equals(appointment.getStartDate()) && availability.getEndDate().after(appointment.getEndDate())) {
            availability.setStartDate(appointment.getEndDate());
            appointment.setAppointmentId(null);
            if ( appointment.isWeekly() || (!appointment.isWeekly() && !isAppointment) ) {
                appointment.setWeeklyId(UUID1);
            }
            appointmentsDB.addOrUpdate(availability);
        } else if (availability.getStartDate().before(appointment.getStartDate()) && availability.getEndDate().after(appointment.getEndDate())) {
            AppointmentsModel newAvailability = availability.clone();
            newAvailability.setStartDate(appointment.getEndDate());
            newAvailability.setEndDate(availability.getEndDate());
            newAvailability.setAppointmentId(null);
            availability.setEndDate(appointment.getStartDate());
            appointment.setAppointmentId(null);
            if ( appointment.isWeekly() || (!appointment.isWeekly() && !isAppointment) ) {
                appointment.setWeeklyId(UUID1);
            }
            newAvailability.setWeeklyId(UUID2);
            appointmentsDB.addOrUpdate(availability);
            appointmentsDB.addOrUpdate(newAvailability);
        }
        if (isAppointment) {
            appointment.setStudentData(student.getUid(), student.getEmail(), student.getDisplayName(), student.getPhotoURL());
        } else {
            appointment.clearStudentData();
        }
        appointmentsDB.addOrUpdate(appointment);
        return appointment;
    }

    @Authenticate
    public Result createAvailability() {
        /* Get user object from request */
        JsonNode json = request().body().asJson();
        /* Get user from json request */
        String uniqueId = UUID.randomUUID().toString();
        AppointmentsModel appointment = new AppointmentsModel();
        appointment.setCoachId(json.findPath("userId").textValue());
        appointment.setStudentId(null);
        appointment.setAppointmentType("");
        appointment.setStartDate(DatatypeConverter.parseDateTime(json.findPath("startDate").textValue()).getTime());
        appointment.setEndDate(DatatypeConverter.parseDateTime(json.findPath("endDate").textValue()).getTime());
        appointment.setAppointmentNotes("");
        appointment.setPresent(false);
        appointment.setServiceType("");
        Boolean weekly = json.findPath("weekly").asBoolean();
        appointment.setWeekly(weekly);
        if (appointment.isWeekly()) {
            appointment.setWeeklyId(uniqueId);
            new Thread(() -> createWeeklyAppointments(json, uniqueId)).start();
        } else {
            appointment.setWeeklyId("");
        }
        appointmentsDB.addOrUpdate(appointment);
        /* Check if user is in DB */

        // new Thread(() -> mailerService.sendAppointmentConfirmation(appointment)).start();

        return ok();
    }

    @Authenticate
    private void createWeeklyAppointments(JsonNode json, String uniqueId) {
        Calendar startDate = DatatypeConverter.parseDateTime(json.findPath("startDate").textValue());
        Calendar currentDate = DatatypeConverter.parseDateTime(json.findPath("startDate").textValue());
        Calendar endDate = DatatypeConverter.parseDateTime(json.findPath("endDate").textValue());
        Calendar semesterEnd = Calendar.getInstance();
        semesterEnd.setTime(settingsDB.get(null).orElseThrow(NullPointerException::new).getSemesterEnd());
        currentDate.add(Calendar.DAY_OF_YEAR, 7);
        endDate.add(Calendar.DAY_OF_YEAR, 7);
        while (currentDate.before(semesterEnd)) {
            AppointmentsModel newAppointment = new AppointmentsModel();
            newAppointment.setCoachId(json.findPath("userId").textValue());
            newAppointment.setStudentId(null);
            newAppointment.setAppointmentType("");

            Calendar startWeeklyDate = Calendar.getInstance();
            startWeeklyDate.setTime(currentDate.getTime());
            Calendar endWeeklyDate = Calendar.getInstance();
            endWeeklyDate.setTime(endDate.getTime());

            if (!TimeZone.getTimeZone( "US/Michigan").inDaylightTime( startDate.getTime() ) && TimeZone.getTimeZone( "US/Michigan").inDaylightTime( currentDate.getTime() )) {
                startWeeklyDate.add(Calendar.HOUR_OF_DAY, -1);
                endWeeklyDate.add(Calendar.HOUR_OF_DAY, -1);
            } else if (TimeZone.getTimeZone( "US/Michigan").inDaylightTime( startDate.getTime() ) && !TimeZone.getTimeZone( "US/Michigan").inDaylightTime( currentDate.getTime() )) {
                startWeeklyDate.add(Calendar.HOUR_OF_DAY, 1);
                endWeeklyDate.add(Calendar.HOUR_OF_DAY, 1);
            }

            newAppointment.setStartDate(startWeeklyDate.getTime());
            newAppointment.setEndDate(endWeeklyDate.getTime());
            newAppointment.setAppointmentNotes("");
            newAppointment.setPresent(false);
            newAppointment.setServiceType("");
            newAppointment.setWeekly(json.findPath("weekly").asBoolean());
            newAppointment.setWeeklyId(uniqueId);
            appointmentsDB.addOrUpdate(newAppointment);
            currentDate.add(Calendar.DAY_OF_YEAR, 7);
            endDate.add(Calendar.DAY_OF_YEAR, 7);
        }
    }

    public Result cancelAppointment() {
        JsonNode json = request().body().asJson();
        String appointmentId = json.findPath("appointmentId").textValue();
        AppointmentsModel appointment = appointmentsDB.get(appointmentId).get();
        appointment.clearStudentData();
        appointmentsDB.addOrUpdate(appointment);
        new Thread(() -> mailerService.sendAppointmentCancellation(appointment, json.findPath("cancelNotes").asText())).start();
        return ok();
    }

    public Result cancelWeeklyAppointment() {
        JsonNode json = request().body().asJson();
        String appointmentId = json.findPath("appointmentId").textValue();
        AppointmentsModel appointment = appointmentsDB.get(appointmentId).get();
        List<AppointmentsModel> appointments = appointmentsDB.getByWeeklyId(appointment.getWeeklyId(), appointment.getStartDate(), appointment.getStudentId());
        appointment.clearStudentData();
        appointmentsDB.addOrUpdate(appointment);
        for ( AppointmentsModel ap : appointments  ) {
            ap.clearStudentData();
            appointmentsDB.addOrUpdate(ap);
        }
        /* TODO add thread to email when canceling weekly appointments */
        return ok();
    }

    public Result removeAppointment() {
        JsonNode json = request().body().asJson();
        String appointmentId = json.findPath("appointmentId").textValue();
        Boolean removeAll = json.findPath("removeAll").asBoolean();
        AppointmentsModel appointment = appointmentsDB.remove(appointmentId).orElseThrow(NullPointerException::new);
        if (appointment.isWeekly() && removeAll) {
            List<AppointmentsModel> appointments = appointmentsDB.getByWeeklyId(appointment.getWeeklyId());
            for (AppointmentsModel ap : appointments) {
                if (ap.getStartDate().after(new Date()) && appointment.getStudentId() == null) {
                    appointmentsDB.remove(ap.getAppointmentId());
                }
            }
        }
        return ok();
    }

    public Result updateCoachNotes() {
        JsonNode json = request().body().asJson();
        String appointmentId = json.findPath("appointmentId").textValue();
        String coachNotes = json.findPath("coachNotes").textValue();
        AppointmentsModel appointment = appointmentsDB.get(appointmentId).orElseThrow(NullPointerException::new);
        appointment.setCoachNotes(coachNotes);
        appointmentsDB.addOrUpdate(appointment);
        return ok();
    }

    public Result availableSlotsForAppointments(String coachId, String start, String end, String service) {
        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calEnd.set(Calendar.HOUR_OF_DAY, 24);
        endDate = calEnd.getTime();
        List<AppointmentsModel> appointments = new ArrayList<>();
        if (coachId.equals("any")) {
            List<UsersModel> coaches = userDB.getCoachesByService(service);
            for ( UsersModel coach : coaches ) {
                appointments.addAll(appointmentsDB.getOpenAppointmentsByUserAndDate(coach.getUid(), startDate, endDate));
            }
        } else {
            appointments = appointmentsDB.getOpenAppointmentsByUserAndDate(coachId, startDate, endDate);
        }
        List<AvailabilityModel> availabilities = new ArrayList<>();
        for ( AppointmentsModel app : appointments ) {
            AvailabilityModel av = new AvailabilityModel(app.getAppointmentId(), app.getCoachId(), app.getStartDate(), app.getEndDate(), app.isWeekly());
            av.setCanBeWeekly(app.isWeekly());
            av.setCanBeOneTime(true);
            if ( app.isWeekly() ) {
                List<AppointmentsModel> weeklyAppointments = appointmentsDB.getByWeeklyId(app.getWeeklyId(), app.getStartDate());
                for (AppointmentsModel appointment : weeklyAppointments){
                    if ( appointment.getStudentId() != null ) {
                        av.setCanBeWeekly(false);
                        break;
                    }
                }
            }
            long duration = av.getEndDate().getTime() - av.getStartDate().getTime();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
            if (diffInMinutes / 30 != 1 && diffInMinutes != 0) {
                for (int i = 0; i < diffInMinutes / 30; i++) {
                    AvailabilityModel newAvail = new AvailabilityModel
                            (av.getavailabilityId(),
                                    app.getCoachId(),
                                    DateUtils.addMinutes(av.getStartDate(), 30 * i),
                                    DateUtils.addMinutes(av.getStartDate(), 30 * (i + 1)),
                                    av.getWeekly()
                            );
                    newAvail.setCanBeWeekly(av.getCanBeWeekly());
                    newAvail.setCanBeOneTime(av.getCanBeOneTime());
                    availabilities.add(newAvail);
                }
            } else {
                availabilities.add(av);
            }
        }

        return ok(Json.toJson(availableSlotsForAny(availabilities)));
    }

    private List<AvailabilityModel> availableSlotsForAny(List<AvailabilityModel> availabilities) {

        List<AvailabilityModel> newAvailabilities = new ArrayList<>();                                     // Creates a new list of availabilities
        for (int i = 0; i < availabilities.size(); i++) {
            AvailabilityModel newAv = new AvailabilityModel(availabilities.get(i).getavailabilityId(),
                    "any",
                    availabilities.get(i).getStartDate(),
                    availabilities.get(i).getEndDate(),
                    false);
            // New availability model where the ID is null, the userID is any, and has the same start and end date as the original availability at i
            for (int j = i; j < availabilities.size(); j++) {                                           // For j from i to the size of the availability list size
                AvailabilityModel currentAv = availabilities.get(j);                                      // Current Availability in the J loop
                if (currentAv.getStartDate().equals(newAv.getStartDate())) {                            // If the current availability in the J loop is at the same time as the availability in the I look that was made
                    if (currentAv.getCanBeWeekly()) {                   // If the availability can be one time and not weekly
                        newAv.addWeeklyUser(currentAv.getavailabilityId());                                       // Add the user id into the array of weekly users
                    }
                    newAv.addOneTimeUser(currentAv.getavailabilityId());
                    availabilities.remove(j);                                                             // Remove the availability from the list of availabilities
                    j--;                                                                                  // Decrease j by one so that the loop goes to the next one
                }
            }
            i--;                                                                                          // Decrease i by one so that the loop goes to the next one
            if (!newAv.getWeeklyUsers().isEmpty()) {                                                    // If the weekly users is not empty
                newAv.setCanBeWeekly(true);                                                               // Set can be weekly to true
                newAv.setCanBeOneTime(true);                                                              // Set can be one time to true
            } else {
                newAv.setCanBeOneTime(true);                                                              // Set can be one time to true
                newAv.setCanBeWeekly(false);                                                              // Set can be weekly to false
            }
            newAvailabilities.add(newAv);                                                                  // Add the new availabilities to the new list of availabilities
        }
        return newAvailabilities;                                                                          // Return the list of new availabilities
    }

    public Result appointmentsForUser(String role, String userId, String start, String end) {
        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();
        Calendar endb = Calendar.getInstance();
        endb.setTime(endDate);
        endb.set(Calendar.HOUR_OF_DAY, 24);
        endDate = endb.getTime();
        List<AppointmentsModel> appointments = appointmentsDB.getAppointmentsByUserAndDate(userId, startDate, endDate);
        return ok(Json.toJson(appointments));
    }

    public Result appointmentsAsCoach(String userId, String start, String end) {
        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();
        Calendar endc = Calendar.getInstance();
        endc.setTime(endDate);
        endc.set(Calendar.HOUR_OF_DAY, 24);
        endDate = endc.getTime();
        List<AppointmentsModel> appointments = appointmentsDB.getCoachAvailablitiliy(userId, startDate, endDate);
        return ok(Json.toJson(appointments));
    }

    @Authenticate(role="Admin")
    public Result appointmentsByDate(String role, String userId, String start, String end) {
        Date startDate = DatatypeConverter.parseDateTime(start).getTime();
        Date endDate = DatatypeConverter.parseDateTime(end).getTime();
        Calendar endc = Calendar.getInstance();
        endc.setTime(endDate);
        endc.set(Calendar.HOUR_OF_DAY, 24);
        endDate = endc.getTime();
        List<AppointmentsModel> appointments = appointmentsDB.getAppointmentsByDate(startDate, endDate);
        return ok(Json.toJson(appointments));
    }
}
