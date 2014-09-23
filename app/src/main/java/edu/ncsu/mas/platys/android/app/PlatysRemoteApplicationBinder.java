package edu.ncsu.mas.platys.android.app;

import android.os.RemoteException;

import java.util.List;

public class PlatysRemoteApplicationBinder extends IPlatysRemoteApplicationService.Stub {

  /**
   * Registers the application with the middleware. It returns immediately with
   * a private key for the application. The application should use the private key for all
   * further communication with the middleware.
   *
   * @param name        Unique name of the application. Platys middleware includes this name
   *                    in all broadcast messages directed to this application.
   * @param description A brief description of the application shown to the user.
   * @return privateKey - private key for the application. Platys generates a unique
   * private key for applications. Applications should use this key for
   * communicating with Platy middleware.
   */
  @Override
  public String registerApplication(String name, String description) throws RemoteException {
    return null;
  }

  /**
   * Unregisters the application.
   *
   * @param privateKey Private key of the applicaion
   */
  @Override
  public void unregisterApplication(String privateKey) throws RemoteException {

  }

  /**
   * Returns the status of the application.
   *
   * @param privateKey Private key of the applicaion
   * @return applicationStatus - applicaion status; one of the values in
   * {@link edu.ncsu.mas.platys.applications.constants.ApplicationStatus} as String.
   */
  @Override
  public String getAplicationStatus(String privateKey) throws RemoteException {
    return null;
  }

  /**
   * Get the current place.
   *
   * @param privateKey Private key of the applicaion
   * @return current place, if user's privacy preferences allow the applications
   * to access it; null, otherwise.
   */
  @Override
  public String getCurrentPlace(String privateKey) throws RemoteException {
    return null;
  }

  /**
   * Get the current activities.
   *
   * @param privateKey Private key of the applicaion
   * @return list of current activities; an empty list is returned if there are
   * no activities the application can access.
   */
  @Override
  public List<String> getCurrentActivities(String privateKey) throws RemoteException {
    return null;
  }

  /**
   * Get the list of all places the application is allowed to access.
   *
   * @param privateKey Private key of the applicaion
   * @return list of all places the application can access.
   */
  @Override
  public List<String> getAllPlaces(String privateKey) throws RemoteException {
    return null;
  }

  /**
   * Get the list of all activities the application is allowed to access.
   *
   * @param privateKey Private key of the applicaion
   * @return list of all activities the application can access.
   */
  @Override
  public List<String> getAllActivities(String privateKey) throws RemoteException {
    return null;
  }

  /**
   * Get the list of social circles a connection (i.e., a friend) of the user
   * belongs to.
   *
   * @param privateKey     Private key of the applicaion
   * @param connectionName Name of connection whose social circles are to be retrieved
   * @return list of social circles to which the connection belongs; an empty
   * list is returned if the social circles are unknown.
   */
  @Override
  public List<String> getSocialCircles(String privateKey, String connectionName) throws RemoteException {
    return null;
  }

  /**
   * Get the list of social circles with which a place or activity information
   * can be shared. Currently, this feature is not implemented and the function
   * returns a list of all social circles.
   *
   * @param privateKey          Private key of the applicaion
   * @param placeOrActivityName Name of a place or an activity
   * @return list of social circles to which the place or the activity
   * information can be shared.
   */
  @Override
  public List<String> getSharableSocialCircles(String privateKey, String placeOrActivityName) throws RemoteException {
    return null;
  }
}
