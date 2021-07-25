package com.example.sacams;

public final class Version {

  private static final String VERSION = "${project.version}";
  private static final String GROUPID = "${project.groupId}";
  private static final String ARTIFACTID = "${project.artifactId}";
  private static final String REVISION = "${buildNumber}";

  /**
   * @return the version
   */
  public static String getVersion() {
    return VERSION;
  }

  /**
   * @return the groupid
   */
  public static String getGroupid() {
    return GROUPID;
  }

  /**
   * @return the artifactid
   */
  public static String getArtifactid() {
    return ARTIFACTID;
  }

  /**
   * @return the revision
   */
  public static String getRevision() {
    return REVISION;
  }

}
