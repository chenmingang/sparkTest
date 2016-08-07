package com.github.chenmingang.util

import java.io.{IOException, InputStream}
import java.util.Properties

/**
  * Created by zeal on 16-7-15.
  */
object ConfigUtil {

  private var properties: Properties = new Properties();

  val inputStream: InputStream = getClass.getResourceAsStream("/app.properties");

  try {
    properties.load(inputStream);
  } catch {
    case e: IOException => e.printStackTrace();
  }

  def getProperty(propertyName: String): String = {
    return properties.getProperty(propertyName)
  }
}
