package tech.lacambra.video;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDateTime;

@RegisterForReflection
public class EncodingRequest {

  public String source;
  public String target;

  public LocalDateTime getTimeStamp() {
    return LocalDateTime.now();
  }

  @Override
  public String toString() {
    return "EncodingRequest{" +
        "source='" + source + '\'' +
        ", target='" + target + '\'' +
        '}';
  }
}
