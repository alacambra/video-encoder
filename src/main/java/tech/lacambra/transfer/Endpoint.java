package tech.lacambra.transfer;

public class Endpoint {

  private String host;
  private int port;

  public Endpoint(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "Endpoint{" +
        "host='" + host + '\'' +
        ", port=" + port +
        '}';
  }
}