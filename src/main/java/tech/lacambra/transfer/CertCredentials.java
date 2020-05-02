package tech.lacambra.transfer;

public class CertCredentials {

  private String userName;
  private String privateKeyPath;
  private String privateKeyPassphrase;

  public CertCredentials(String userName, String privateKeyPath, String privateKeyPassphrase) {
    this.userName = userName;
    this.privateKeyPath = privateKeyPath;
    this.privateKeyPassphrase = privateKeyPassphrase;
  }

  public String getUserName() {
    return userName;
  }

  public String getPrivateKeyPath() {
    return privateKeyPath;
  }

  public String getPrivateKeyPassphrase() {
    return "\"\"".equals(privateKeyPassphrase) ? null : privateKeyPassphrase;
  }
}
