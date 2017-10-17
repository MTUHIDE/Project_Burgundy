package models;
import java.util.Date;
//should setters pass info back to database (or however that works)

public class AppointmentsModel {
    private String appointmentName;
    private String appointmentId;
    private Date startDate;
    private Date endDate;
    private String studentName;
    private String coachName;
    private String appointmentNotes;

    public String getAppointmentName() { return appointmentName; }

    public void setAppointmentName(String appointmentName) { this.appointmentName = appointmentName; }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCoachName() {
        return coachName;
    }

    public void setCoachName(String coachName) {
        this.coachName = coachName;
    }

    public String getAppointmentNotes() {
        return appointmentNotes;
    }

    public void setAppointmentNotes(String appointmentNotes) {
        this.appointmentNotes = appointmentNotes;
    }

    //should this just be what the constructor does?
    public void loadData(JSON data) {

        appointmentName = data.getString("appointmentName");
        appointmentId = data.getString("appointmentId");
        studentName = data.getString("studentName");
        coachName = data.getString("coachName");
        appointmentNotes = data.getString("appointmentNotes");

        String startDateStr = data.getString("startDate");
        String endDateStr = data.getString("endDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        startDate = sdf.parse(startDateStr);
        endDate = sdf.parse(endDateStr);
    }
}
