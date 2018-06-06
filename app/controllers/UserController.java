package controllers;

import application_components.annotations.Authenticate;
import com.fasterxml.jackson.databind.JsonNode;
import databases.UserDB;
import models.ServiceModel;
import models.UsersModel;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UserController extends Controller {
    /* Roles used throughout TimeSlot */
    private String[] roles = { "Student" , "Coach", "Admin" };
    private UserDB userDB = new UserDB();

    public Result index() {
        String currentRole = getCurrentRole();
        /* Force redirect to Login is the user isn't signed in */
        if (currentRole == null) {
            return ok(views.html.pages.login.render());
        }
        return ok(views.html.pages.users.render());
    }

    public Result userPage(String userId) {
        if (userId == null) return notFound();
        Optional<UsersModel> user = userDB.get(userId);
        if (user == null) return notFound();
        return ok(views.html.pages.user.render(user.orElseThrow(NullPointerException::new)));
    }

    public Result updateUser() {
        /* Get user object from request */
        JsonNode json = request().body().asJson();
        /* Get user from json request */
        UsersModel user = Json.fromJson(json, UsersModel.class);
        /* Check if user is in DB */
        userDB.addOrUpdate(user);
        return ok();
    }

    @Authenticate(role="Admin")
    public Result updateUserRole() {
        /* Get user object from request */
        JsonNode json = request().body().asJson();
        /* Get user from json request */
        String userId = json.get("uid").asText();
        String role = json.get("role").asText();
        /* Check if user is in DB */
        Optional<UsersModel> u = userDB.get(userId);
        UsersModel user = u.orElseThrow(NullPointerException::new);
        user.setRole(role);
        userDB.addOrUpdate(user);
        return ok();
    }

    @Authenticate(role="Coach")
    public Result addServiceToCoach() {
        JsonNode json = request().body().asJson();
        String userId = json.get("userId").asText();
        String serviceText = json.get("serviceName").asText();
        String serviceId = json.get("serviceId").asText();
        ServiceModel service = new ServiceModel(serviceId, serviceText);
        userDB.addServiceToUser(userId, service);
        return ok();
    }

    @Authenticate(role="Coach")
    public Result removeServiceFromCoach() {
        JsonNode json = request().body().asJson();
        String userId = json.get("userId").asText();
        String serviceId = json.get("serviceId").asText();
        userDB.removeServiceFromUser(userId, serviceId);
        return ok();
    }

    @Authenticate(role="Admin")
    public Result removeUser() {
        try {
            JsonNode json = request().body().asJson();
            String userId = json.get("userId").asText();
            if (userDB.remove(userId) != null) {
                return ok();
            } else {
                return internalServerError();
            }
        } catch (Exception e) {
            Logger.debug(e.getMessage());
            return internalServerError();
        }
    }

    public Result getCoachesByService(String serviceId) {
        List<UsersModel> coaches = userDB.getCoachesByService(serviceId);
        coaches.sort(Comparator.comparing(UsersModel::getDisplayName));
        return ok(Json.toJson(coaches));
    }

    public UsersModel getCurrentUser() {
        String s = session("currentUser");
        if (s == null || s.isEmpty()) {
            UsersModel u = new UsersModel();
            u.setRole("Student");
            return u;
        }
        return new UserDB().get(s).orElseThrow(NullPointerException::new);
    }

    public void addAttributes(UsersModel user, String... attributes) {
        List<String> o = user.getAttributes();
        o.addAll(Arrays.asList(attributes));
        user.setAttributes(o);
    }

    public void removeAttribute(UsersModel user, String attribute) {
        List<String> o = user.getAttributes();
        o.remove(attribute);
        user.setAttributes(o);
    }

    public boolean hasAttribute(UsersModel user, String attribute) {
        List<String> attributes = user.getAttributes();
        return (attributes.contains(attribute));
    }

    public String getCurrentRole() {
        return session("currentRole");
    }

    public String[] getRoles() {
        return roles;
    }
}
