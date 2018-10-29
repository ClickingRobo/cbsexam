package com.cbsexam;

import com.google.gson.Gson;
import controllers.DatabaseController;
import controllers.UserController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  private static Connection connection;


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

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserController.getUsers();

    // TODO: Add Encryption to JSON (FIXED)
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);

    //Added encryption
    //json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
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

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to delete users (FIXED)
  @DELETE
  @Path("/{idUser}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("idUser") int idUser) {


    User user = UserController.getUser(idUser);

    User deletedUser = UserController.deleteUser(user);
    if (deletedUser != null) {

      String out = new Gson().toJson("Nu er din user slettet");

      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(out).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to update users
  public Response updateUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }
}
