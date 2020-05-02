package tech.lacambra;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import tech.lacambra.transfer.SftpClient;
import tech.lacambra.video.Encoder;
import tech.lacambra.video.EncodingRequest;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Path("/encode")
public class Server {

  private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

  @Inject
  Encoder encoder;

  @Inject
  ManagedExecutor mes;

  @Inject
  Instance<SftpClient> sftpClientInstances;

  @Inject
  @ConfigProperty(name = "tech.lacambra.video.encode.remote.waiting.path")
  String waitingPath;

  @Inject
  @ConfigProperty(name = "tech.lacambra.video.encode.remote.encoded.path")
  String encodedPath;

  @Inject
  @ConfigProperty(name = "tech.lacambra.video.encode.local.processing.path")
  String processingPath;

  static {
    java.nio.file.Path p = Paths.get("config");
    LOGGER.info("[init] Path=" + p.toAbsolutePath().toString());

    p = Paths.get("config", "application.properties");
    LOGGER.info("[init] ap path: " + p);
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response encodeVideo(EncodingRequest encodingRequest) {

    try (SftpClient sftpClient = sftpClientInstances.get()) {
      sftpClient.connect();
      sftpClient.saveFileLocally(waitingPath.concat(encodingRequest.source), processingPath, encodingRequest.source);
      CompletableFuture.supplyAsync(() -> {
        startEncoding(encodingRequest.source, encodingRequest.target);
        return null;
      }, mes).exceptionally(ex -> {
        LOGGER.severe("[encodeVideo] Error: " + ex.getMessage());
        return null;
      });
      mes.execute(() -> startEncoding(encodingRequest.source, encodingRequest.target));
    }

    return Response.ok(encodingRequest).build();
  }

  @POST
  @Path("up")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  public void uploadFiles(InputStream stream) {
    try (SftpClient sftpClient = sftpClientInstances.get()) {
      sftpClient.connect();
      if (!sftpClient.pathExists("/downloader-albert/test")) {
        sftpClient.createDir("/downloader-albert/test");
      }

//      sftpClient.uploadFile("/downloader-albert/test/t.txt", stream);
    }
  }

  @GET
  @Path("encode")
  public void encode(@QueryParam("fileName") String fileName) {


    try (SftpClient sftpClient = sftpClientInstances.get()) {
      sftpClient.connect();
      sftpClient.saveFileLocally(waitingPath.concat(fileName), processingPath, fileName);
      mes.execute(() -> startEncoding(fileName, fileName + ".out"));
    }

//  @GET
//  public JsonArray list() {
//    return sftpClient.listDir("/downloader-albert").stream().map(Json::createValue).collect(JsonCollectors.toJsonArray());
//  }
  }

  private void startEncoding(String fileName, String targetFileName) {

    LOGGER.info("[startEncoding] Encoding file " + fileName);

    encoder.encode(processingPath + fileName, processingPath, targetFileName);

    LOGGER.info("[startEncoding] Encoding done");

    try (SftpClient sftpClient = sftpClientInstances.get()) {
      LOGGER.info("[startEncoding] Connecting sftp done");
      sftpClient.connect();
      LOGGER.info(String.format("[startEncoding] Uploading file. remotePath=%s, sourcePath=%s, fileName=%s", encodedPath, Paths.get(processingPath + fileName), targetFileName));
      sftpClient.uploadFile(encodedPath, Paths.get(processingPath + targetFileName), targetFileName);
      LOGGER.info("[startEncoding] File uploaded");
    }
  }
}
