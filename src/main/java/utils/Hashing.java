package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


import org.bouncycastle.util.encoders.Hex;

public final class Hashing {

  //Setting salt string
  private String salt = "abcdefghijklmnopqrstuvxyz1234567890";

  //Obtaining current time with millisecond precision for salting
  private String currentTimeSalt = Long.toString(System.currentTimeMillis());


  // TODO: You should add a salt and make this secure (FIXED)
  public static String md5(String rawString, String salt) {

    try {

      // We load the hashing algoritm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

      //adds the salt to the rawstring so that the hashing is done on the password and salt values
      rawString += salt;

      // We convert to byte array
      byte[] byteArray = md.digest(rawString.getBytes());

      // Initialize a string buffer
      StringBuffer sb = new StringBuffer();

      // Run through byteArray one element at a time and append the value to our stringBuffer
      for (int i = 0; i < byteArray.length; ++i) {
        sb.append(Integer.toHexString((byteArray[i] & 0xFF) | 0x100).substring(1, 3));
      }

      //Convert back to a single string and return
      return sb.toString();

    } catch (java.security.NoSuchAlgorithmException e) {

      //If somethings breaks
      System.out.println("Could not hash string");
    }

    return null;
  }



  // TODO: You should add a salt and make this secure (FIXED)
  public static String sha(String rawString, String salt) {
    try {
      // We load the hashing algoritm we wish to use.
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      //adds the salt to the rawstring so that the hashing is done on the password and salt values
      rawString += salt;

      // We convert to byte array
      byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

      // We create the hashed string
      String sha256hex = new String(Hex.encode(hash));

      // And return the string
      return sha256hex;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return rawString;
  }


  //----------------------------------------------------------------------------------------------
  //References: YouTube: Java Basics - StringBuilder - https://www.youtube.com/watch?v=MxfmXw2O64E
  //YouTube: How to generate random numeric[...] https://www.youtube.com/watch?v=kgx33gkBPWI
  private static String randomizeSalt(String salt){
    Random random = new Random();
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++){
      sb.append(salt.charAt(random.nextInt(salt.length())));
    }
    return sb.toString();
  }

  //Performs randomization of salt String variable
  public String performSalt (String salt){
    return Hashing.randomizeSalt(salt);
  }

  public void setSalt (String salt, String currentTimeSalt){
    this.salt = performSalt(salt);
    this.currentTimeSalt = currentTimeSalt;
  }

  public String combiningSalts (){
    setSalt(salt, currentTimeSalt);
    String combinedSalt = salt + currentTimeSalt;
    return combinedSalt;
  }
}

//--------------------