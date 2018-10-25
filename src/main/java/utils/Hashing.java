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





  // TODO: You should add a salt and make this secure (USING SHA-2 FOR NOW)
  public static String md5(String rawString) {

    try {

      // We load the hashing algoritm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

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
  public static String sha(String rawString) {
    try {
      // We load the hashing algoritm we wish to use.
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

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



  //Hashes with sha-2 algorithm
  public String performHash (String str){
    return Hashing.sha(str);
  }

  //Performs randomization of salt String variable
  public String performSalt (String salt){
    return Hashing.randomizeSalt(salt);
  }

  public void setSalt (String salt, String currentTimeSalt){
    this.salt = performSalt(salt);
    this.currentTimeSalt = currentTimeSalt;
  }

  //Sets salt and then applies the hashing of the string + salt variables
  public String hashAndSalt (String str){
    setSalt(salt,currentTimeSalt);
    String newString = str + this.salt + this.currentTimeSalt;
    return performHash(newString);
  }
}

//--------------------