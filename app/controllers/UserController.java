package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.Databases.UserDB;
import models.UsersModel;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

public class UserController extends Controller {
    public Result index() {
        return ok(views.html.coaches.render());
    }

    public Result updateUser() {
        /* Get user object from request */
        JsonNode json = request().body().asJson();
        /* Get user from json request */
        UsersModel user = Json.fromJson(json, UsersModel.class);
        /* Check if user is in DB */
        UserDB.addUser(user);
        return ok();
    }

    public Result removeUser() {
        try {
            JsonNode json = request().body().asJson();
            String userId = json.get("userId").asText();
            if(UserDB.removeUser(userId)){
                return ok();
            } else {
                return internalServerError();
            }
       } catch(Exception e) {
            Logger.debug(e.getMessage());
            return internalServerError();
        }
    }

    public static UsersModel getCurrentUser() {
        String s = session().get("currentUser");
        return UserDB.getUser(s);
    }
}
