package controllers;

import application_components.Application;
import databases.AppointmentsDB;
import models.AppointmentsModel;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;

public class ReportsController extends Controller {
    AppointmentsDB appointmentsDB = new AppointmentsDB();
    Application application = new Application();

    public Result index() {
        application.restrictByRole("Admin");
        return ok(views.html.reports.render());
    }

    public Result getAppointmentDate(Long reportStart, Long reportEnd) {
        application.restrictByRole("Admin");
        Date start = new Date(reportStart);
        Date end = new Date(reportEnd);
        List<AppointmentsModel> appointments = appointmentsDB.getAppointmentsByDate(start, end);
        return ok(Json.toJson(appointments));
    }
}
