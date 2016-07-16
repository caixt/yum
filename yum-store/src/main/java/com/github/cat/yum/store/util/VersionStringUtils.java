package com.github.cat.yum.store.util;


import java.util.Comparator;
import org.apache.commons.lang.StringUtils;

public class VersionStringUtils
{
  public static final String DELIMITER_PATTERN = "[\\.,-]";
  public static final String COMPONENT_PATTERN = "[\\d\\w]+";
  public static final String VALID_VERSION_PATTERN = "[\\d\\w]+([\\.,-][\\d\\w]+)*";
  
  public static boolean isValidVersionString(String version)
  {
    return (version != null) && (version.matches("[\\d\\w]+([\\.,-][\\d\\w]+)*"));
  }
  
  public static int compare(String version1, String version2)
  {
    String thisVersion = "0";
    if (StringUtils.isNotEmpty(version1)) {
      thisVersion = version1.replaceAll(" ", "");
    }
    String compareVersion = "0";
    if (StringUtils.isNotEmpty(version2)) {
      compareVersion = version2.replaceAll(" ", "");
    }
    if ((!thisVersion.matches("[\\d\\w]+([\\.,-][\\d\\w]+)*")) || (!compareVersion.matches("[\\d\\w]+([\\.,-][\\d\\w]+)*"))) {
      throw new IllegalArgumentException("Version number '" + thisVersion + "' cannot be compared to '" + compareVersion + "'");
    }
    String[] v1 = thisVersion.split("[\\.,-]");
    String[] v2 = compareVersion.split("[\\.,-]");
    
    Comparator<String> componentComparator = new VersionStringComponent();
    for (int i = 0; i < (v1.length > v2.length ? v1.length : v2.length); i++)
    {
      String component1 = i >= v1.length ? "0" : v1[i];
      String component2 = i >= v2.length ? "0" : v2[i];
      if (componentComparator.compare(component1, component2) != 0) {
        return componentComparator.compare(component1, component2);
      }
    }
    return 0;
  }
  
  private static class VersionStringComponent
    implements Comparator<String>
  {
    
    private VersionStringComponent() {}
    
    public int compare(String component1, String component2)
    {
      if (component1.equalsIgnoreCase(component2)) {
        return 0;
      }
      component1 = component1.replaceAll("[a-zA-Z]", "");
      component2 = component2.replaceAll("[a-zA-Z]", "");
      
      if ((isInteger(component1)) && (isInteger(component2)))
      {
        if (Integer.parseInt(component1) > Integer.parseInt(component2)) {
          return 1;
        }
        if (Integer.parseInt(component2) > Integer.parseInt(component1)) {
          return -1;
        }
        return 0;
      }
      if ("0".equals(component1)) {
        return 1;
      }
      if ("0".equals(component2)) {
        return -1;
      }
      if ((isInteger(component1)) && (component2.startsWith(component1))) {
        return 1;
      }
      if ((isInteger(component2)) && (component1.startsWith(component2))) {
        return -1;
      }
      return component1.compareToIgnoreCase(component2);
    }
    
    private boolean isInteger(String string)
    {
      return string.matches("\\d+");
    }
  }
}

