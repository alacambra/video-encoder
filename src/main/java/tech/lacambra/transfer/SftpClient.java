package tech.lacambra.transfer;

import com.jcraft.jsch.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Dependent
public class SftpClient implements AutoCloseable {

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.username")
  String userName;

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.host")
  String host;

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.port")
  String port;

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.privateKeyPath")
  String privateKeyPath;

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.privateKeyPassphrase")
  String privateKeyPassphrase;

  Session session;
  ChannelSftp channelSftp;

  private static final Logger LOGGER = Logger.getLogger(SftpClient.class.getName());

//  public Collection<String> listDir(String path) {
//
//    ChannelSftp channelSftp = null;
//    channelSftp = createChannelSftp();
//
//    try {
//
//      Vector<ChannelSftp.LsEntry> v = channelSftp.ls("/downloader-albert");
//      System.out.println(pathExists("/downloader-albert"));
//      isFile(path);
//
//      return v.stream().map(ChannelSftp.LsEntry::getFilename).collect(Collectors.toList());
//    } catch (SftpException e) {
//      throw new RuntimeException(e);
//    } finally {
//      if (channelSftp != null && !channelSftp.isClosed()) {
//        channelSftp.disconnect();
//      }
//    }
//  }

  public boolean pathExists(String path) {
    try {
      channelSftp.lstat(path);
      return true;
    } catch (SftpException e) {
      if (e.id == 2) {
        return false;
      }

      throw new RuntimeException(e);
    }
  }

  public boolean isFile(String path) {
    return !isDir(path);
  }

  public boolean isDir(String path) {

    try {
      if (!pathExists(path)) {
        throw new RuntimeException("Path does not exists");
      }

      return channelSftp.lstat(path).isDir();

    } catch (SftpException e) {
      throw new RuntimeException(e);
    }
  }

  public void uploadFile(String remotePath, Path sourcePath, String fileName) {

    try (InputStream is = Files.newInputStream(sourcePath)) {
      channelSftp.put(is, remotePath + fileName);
    } catch (SftpException | IOException e) {
      throw new RuntimeException(e);
    }
  }


  public boolean createDir(String path) {
    try {
      if (pathExists(path)) {
        throw new RuntimeException("Already exists");
      }
      channelSftp.mkdir(path);
      return true;
    } catch (SftpException e) {
      if (e.id == 2) {
        return false;
      }

      throw new RuntimeException(e);
    }
  }

  public void saveFileLocally(String remoteSource, String localTargetDir, String targetName) {

    if (!Files.exists(Paths.get(localTargetDir))) {
      throw new RuntimeException("Invalid target given");
    }

    if (!pathExists(remoteSource) || !isFile(remoteSource)) {
      throw new RuntimeException("Invalid source give");
    }

    Path target = Paths.get(localTargetDir).resolve(targetName);

    try (InputStream is = channelSftp.get(remoteSource); FileOutputStream fos = new FileOutputStream(target.toFile())) {

      is.transferTo(fos);

    } catch (SftpException | IOException e) {
      throw new RuntimeException(e);
    }

  }

  public void moveFile(String src, String target) {

    if (!pathExists(src) || !isFile(src)) {
      throw new RuntimeException("Src not found or not a file");
    }

    if (!pathExists(target)) {
      throw new RuntimeException("Target not found");
    }

    try {
      channelSftp.put(src, target);
    } catch (SftpException e) {
      throw new RuntimeException(e);
    }

  }

  public void connect() {

    if (channelSftp != null && channelSftp.isConnected()) {
      return;
    }

    getSession();

    try {

      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();

    } catch (JSchException e) {
      if (channelSftp != null && !channelSftp.isClosed()) {
        channelSftp.disconnect();
      }
      session.disconnect();
      throw new RuntimeException(e);
    }
  }

  public Session getSession() {

    JSch jSch = new JSch();
    Endpoint endpoint = new Endpoint(host, Integer.parseInt(port));
    CertCredentials certCredentials = new CertCredentials(userName, privateKeyPath, privateKeyPassphrase);

    session = createSession(jSch, endpoint, certCredentials);

    return session;
  }

  private Session createSession(JSch jSch, Endpoint endpoint, CertCredentials certCredentials) {

    if (session != null && session.isConnected()) {
      return session;
    }

    try {
      if (certCredentials.getPrivateKeyPassphrase() == null) {
        jSch.addIdentity(certCredentials.getPrivateKeyPath());
      } else {
        jSch.addIdentity(certCredentials.getPrivateKeyPath(), certCredentials.getPrivateKeyPassphrase());
      }

      Session session = jSch.getSession(certCredentials.getUserName(), endpoint.getHost(), endpoint.getPort());

      session.setConfig("StrictHostKeyChecking", "no");

      LOGGER.info("[createSession] Trying to connect. Endpoint=" + endpoint + ", username=" + certCredentials.getUserName());

      session.connect();
      LOGGER.info("[createSession] Connection established. SessionConnected=" + session.isConnected());

      return session;

    } catch (JSchException e) {
      if (session != null && session.isConnected()) {
        session.disconnect();
      }
      throw new RuntimeException(e);
    }
  }

  public void closeClient() {

    if (channelSftp != null && channelSftp.isConnected()) {
      channelSftp.disconnect();
    }

    if (session != null && session.isConnected()) {
      session.disconnect();
    }
  }

  @Override
  public void close() {
    closeClient();
  }
}
