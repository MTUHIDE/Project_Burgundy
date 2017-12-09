package controllers.Databases;

import com.google.api.Service;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import controllers.ApplicationComponents.Roles;
import models.AppointmentTypeModel;
import models.ServiceModel;
import models.SettingsModel;
import models.UsersModel;
import play.api.Play;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* DB classes contain the methods necessary to manage their corresponding models.
* UserDB works with UsersModel to retrieve and remove users in the Firestore DB.*/
public class SettingsDB {


    public static synchronized SettingsModel getSettings() {
        DocumentReference docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings");
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        SettingsModel settings = null;
        assert document != null;
        if (document.exists()) {
            settings = document.toObject(SettingsModel.class);
        }
        settings = SettingsModel.replaceNull(settings);
        return settings;
    }

    public static boolean removeAppointmentType(String appointmentTypeId) {
        /* Asynchronously remove user from DB */
        ApiFuture<WriteResult> writeResult = FirestoreDB.getFirestoreDB()
                .collection("settings")
                .document("settings")
                .collection("appointmentTypes")
                .document(appointmentTypeId).delete();
        try {
            /* Verify that action is complete */
            writeResult.get();
            return writeResult.isDone();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized void addAppointmentType(AppointmentTypeModel appointmentType){
        DocumentReference docRef;
        if(appointmentType.getAppointmentTypeId() == null) {
            docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("appointmentTypes").document();
        } else {
            docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("appointmentTypes").document(appointmentType.getAppointmentTypeId());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("appointmentType", appointmentType.getAppointmentType());
        ApiFuture<WriteResult> result = docRef.set(data);
        result.isDone();
    }

    public static synchronized List<AppointmentTypeModel> getAppointmentTypes(){
        List<AppointmentTypeModel> appointmentTypesList = new ArrayList<>();
        /* Asynchronously retrieve all users */
        ApiFuture<QuerySnapshot> query = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("appointmentTypes").get();
        QuerySnapshot querySnapshot = null;
        try {
            /* Attempt to get a list of all users - blocking */
            querySnapshot = query.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert querySnapshot != null;
        List<DocumentSnapshot> documents = querySnapshot.getDocuments();
        /* Iterate users and add them to a list for return */
        for (DocumentSnapshot document : documents) {
                AppointmentTypeModel appointmentType = new AppointmentTypeModel(
                        document.getId(),
                        document.getString("appointmentType")
                );
                appointmentTypesList.add(appointmentType);
            }
        return appointmentTypesList;
    }

    public static synchronized void addService(ServiceModel service) {
        DocumentReference docRef;
        if(service.getServiceId() == null) {
            docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("services").document();
        } else {
            docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("services").document(service.getServiceId());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("service", service.getService());
        ApiFuture<WriteResult> result = docRef.set(data);
        result.isDone();
    }

    public static synchronized boolean removeService(String serviceID) {
        ApiFuture<WriteResult> writeResult = FirestoreDB.getFirestoreDB()
                .collection("settings")
                .document("settings")
                .collection("services")
                .document(serviceID).delete();
        try {
            /* Verify that action is complete */
            writeResult.get();
            return writeResult.isDone();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static synchronized List<ServiceModel> getServices(){
        List<ServiceModel> serviceModel = new ArrayList<>();
        /* Asynchronously retrieve all users */
        ApiFuture<QuerySnapshot> query = FirestoreDB.getFirestoreDB().collection("settings").document("settings").collection("services").get();
        QuerySnapshot querySnapshot = null;
        try {
            /* Attempt to get a list of all users - blocking */
            querySnapshot = query.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        assert querySnapshot != null;
        List<DocumentSnapshot> documents = querySnapshot.getDocuments();
        /* Iterate users and add them to a list for return */
        for (DocumentSnapshot document : documents) {
            ServiceModel service = new ServiceModel(
                    document.getId(),
                    document.getString("service")
            );
            serviceModel.add(service);
        }
        return serviceModel;
    }

    public static synchronized void changeSettings(SettingsModel settings) {
        DocumentReference docRef = FirestoreDB.getFirestoreDB().collection("settings").document("settings");
        Map<String, Object> data = new HashMap<>();
        data.put("universityName", settings.getUniversityName());
        data.put("centerName", settings.getCenterName());
        /* Verify that colors are valid */
        Pattern pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
        Matcher matcher = pattern.matcher(settings.getPrimaryColor());
        if(matcher.matches()){
            data.put("primaryColor",settings.getPrimaryColor());
        } else {
            settings.setPrimaryColor("#000000");
            data.put("primaryColor", settings.getPrimaryColor());
        }
        matcher = pattern.matcher(settings.getSecondaryColor());
        if(matcher.matches()){
            data.put("secondaryColor",settings.getSecondaryColor());
        } else {
            settings.setSecondaryColor("#ffffff");
            data.put("secondaryColor", settings.getSecondaryColor());
        }
        /* Write settings to DB */
        ApiFuture<WriteResult> result = docRef.set(data);
        result.isDone();
    }
}
