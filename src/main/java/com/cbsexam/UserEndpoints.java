package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  //has to be static so it can be saved between requests and not updated every single time
  static UserCache userCache = new UserCache();


  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON (FIXED)
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);

    //Added encryption
    json = Encryption.encryptDecryptXOR(json);

    // TODO: What should happen if something breaks down? (FIXED)
    if (user != null){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not get user through the ID provided").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users with cache
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON (FIXED)
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //Added encryption
    //json = Encryption.encryptDecryptXOR(json);

    if (users != null){
      // Return the users with the status code 200
      return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    } else {
      return Response.status(400).entity("Could not retrieve users").build();
    }
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system (FIXED).
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    User newUser = new Gson().fromJson(x, User.class);

    String token = UserController.userAuthentication(newUser);

    if (token != null) {

        newUser.setToken(token);

        String out = new Gson().toJson(newUser);

      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(out).build();
    } else {
      return Response.status(400).entity("Could not log user in").build();
    }
  }

  // TODO: Make the system able to delete users (FIXED)
  @DELETE
  @Path("/")
  //@Consumes(MediaType.APPLICATION_JSON)
  //Takes the token through body instead of as a parameter
  public Response deleteUser(String token) {

    Boolean deletedUser = UserController.deleteUser(token);

    if (deletedUser == true) {

      String out = new Gson().toJson("Your user has now been deleted");

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(out).build();
    } else {
      return Response.status(400).entity("Your user COULD NOT be deleted").build();
    }
  }

  /*

  @POST
  @Path("/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("idUser") int idUser) {

    User user = UserController.getUser(idUser);

    User updatedUser = UserController.updateUser(user);

    if (updatedUser !=null) {

      String out = new Gson().toJson("Your user has now been updated");

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(out).build();
    } else {
      return Response.status(400).entity("Your user COULD NOT be updated").build();
    }
    */

  // TODO: Make the system able to update users(FIXED)
  @POST
  @Path("/update")
  //@Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(String body) {

    //User updatedUser = new Gson().fromJson(token, User.class);

    //SOURCE: https://stackoverflow.com/questions/4110664/gson-directly-convert-string-to-jsonobject-no-pojo
    //JsonParser
    JsonParser parser = new JsonParser();
    //converts String value of body to JsonObject
    JsonObject o = parser.parse(body).getAsJsonObject();
    //Obtains the String value of the token
    String token = new Gson().fromJson(o.get("token"), String.class);
    //Obtains the Object value of the user
    User u = new Gson().fromJson(o.get("user"), User.class);
    //This is done to seperate the user object and the token string from one another

    //both parameters (token and user is passed)
    Boolean isUpdated = UserController.updateUser(token, u);

    if (isUpdated == true) {

      String out = new Gson().toJson("Your user has now been updated");

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(out).build();
    } else {
      return Response.status(400).entity("Your user COULD NOT be updated").build();
    }



  }








}
