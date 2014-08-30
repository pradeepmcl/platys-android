// IPlatysAppService.aidl
package edu.ncsu.mas.platys.android.app;

// Declare any non-default types here with import statements

interface IPlatysAppService {
  /**
   * Registers the application with the middleware. It returns immediately with
   * a private key for the application. The application should use the private key for all
   * further communication with the middleware.
   *
   * @param name
   *          Unique name of the application. Platys middleware includes this name
   *          in all broadcast messages directed to this application.
   * @param description
   *          A brief description of the application shown to the user.
   * @return privateKey - private key for the application. Platys generates a unique
   *         private key for applications. Applications should use this key for
   *         communicating with Platy middleware.
   */
  String registerApplication(String name, String description);

  /**
   * Unregisters the application.
   *
   * @param privateKey
   *          Private key of the applicaion
   */
  void unregisterApplication(String privateKey);
}
