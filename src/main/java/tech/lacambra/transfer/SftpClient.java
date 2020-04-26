package tech.lacambra.transfer;

import com.jcraft.jsch.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Dependent
public class SftpClient {

  @Inject
  @ConfigProperty(name = "tech.lacambra.sftp.props")
  String propsPath;

  private static final Logger LOGGER = Logger.getLogger(SftpClient.class.getName());

  public Collection<String> listDir(String path) {

    ChannelSftp channelSftp = null;
    try {
      channelSftp = createChannelSftp();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
//      channelSftp.mkdir("/music/downloader/removeme");
      Vector<ChannelSftp.LsEntry> v = channelSftp.ls("/downloader-albert");
      System.out.println(pathExists("/downloader-albert", channelSftp));
      isFile(path, channelSftp);

      return v.stream().map(ChannelSftp.LsEntry::getFilename).collect(Collectors.toList());
//
//      for (Object o : v) {
//        System.out.println(o);
//      }

//      channelSftp.put(
//          Files.newInputStream(Paths.get("/Users/albertlacambra/git/lacambra.tech/downloader/Hora 25 (20_12_2019 - Tramo de 21_00 a 22_00).mp3")),
//          "/music/downloader/hannah/test.mp3");
    } catch (SftpException e) {
      throw new RuntimeException(e);
    } finally {
      if (channelSftp != null && !channelSftp.isClosed()) {
        channelSftp.disconnect();
      }
    }
  }

  public boolean pathExists(String path, ChannelSftp channelSftp) {
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

  public boolean isFile(String path, ChannelSftp channelSftp) {
    return !isDir(path, channelSftp);
  }

  public boolean isDir(String path, ChannelSftp channelSftp) {

    try {
      if (!pathExists(path, channelSftp)) {
        throw new RuntimeException("Path does not exists");
      }

      return channelSftp.lstat(path).isDir();

    } catch (SftpException e) {
      throw new RuntimeException(e);
    }

  }

  public boolean createDir(String path, ChannelSftp channelSftp) throws SftpException {
    try {
      if (pathExists(path, channelSftp)) {
        throw new RuntimeException("Already exists");
      }

      channelSftp.lstat(path);
      return true;
    } catch (SftpException e) {
      if (e.id == 2) {
        return false;
      }

      throw e;
    }
  }

  public void moveFile(String src, String target, ChannelSftp channelSftp) {

    if (!pathExists(src, channelSftp) || !isFile(src, channelSftp)) {
      throw new RuntimeException("Src not found or not a file");
    }

    if (!pathExists(target, channelSftp)) {
      throw new RuntimeException("Target not found");
    }

    try {
      channelSftp.put(src, target);
    } catch (SftpException e) {
      throw new RuntimeException(e);
    }

  }

  private ChannelSftp createChannelSftp() throws IOException {

    ChannelSftp channelSftp = null;
    Session session = getSession();

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
    return channelSftp;
  }

  public Session getSession() throws IOException {

    BufferedReader reader = Files.newBufferedReader(Paths.get(propsPath));
    Properties p = new Properties();
    p.load(reader);

    JSch jSch = new JSch();
    Endpoint endpoint = new Endpoint(p.getProperty("host"), Integer.parseInt(p.getProperty("port")));
    CertCredentials certCredentials = new CertCredentials(p.getProperty("username"), p.getProperty("privateKeyPath"), p.getProperty("privateKeyPassphrase"));

    Session session = createSession(jSch, endpoint, certCredentials);

    return session;
  }

  public Session createSession(JSch jSch, Endpoint endpoint, CertCredentials certCredentials) {
    try {
      jSch.addIdentity(certCredentials.getPrivateKeyPath(), certCredentials.getPrivateKeyPassphrase());
      Session session = jSch.getSession(certCredentials.getUserName(), endpoint.getHost(), endpoint.getPort());

      session.setConfig("StrictHostKeyChecking", "no");

      LOGGER.info("[createSession] Trying to connect. Endpoint=" + endpoint + ", username=" + certCredentials.getUserName());

      session.connect();
      LOGGER.info("[createSession] Connection established. SessionConnected=" + session.isConnected());

      return session;

    } catch (JSchException e) {
      throw new RuntimeException(e);
    }
  }
}
