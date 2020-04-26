package tech.lacambra.video;

import io.vertx.core.Vertx;
import org.eclipse.microprofile.context.ManagedExecutor;
import tech.lacambra.transfer.SftpClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.stream.JsonCollectors;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/encode")
public class Server {

  @Inject
  Vertx vertx;

  @Inject
  Encoder encoder;

  @Inject
  ManagedExecutor exec;

  @Inject
  SftpClient sftpClient;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response encodeVideo(EncodingRequest encodingRequest) throws IOException {

    exec.execute(encoder::encode);

    return Response.ok(encodingRequest).build();
  }

  @GET
  public JsonArray list() {
    return sftpClient.listDir("/downloader-albert").stream().map(Json::createValue).collect(JsonCollectors.toJsonArray());
  }

  @PostConstruct
  public void init() {
  }
}
