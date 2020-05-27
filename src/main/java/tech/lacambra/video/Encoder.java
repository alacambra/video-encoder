package tech.lacambra.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Logger;

@Dependent
public class Encoder {

  private static final Logger LOGGER = Logger.getLogger(Encoder.class.getName());

  @Inject
  @ConfigProperty(name = "tech.lacambra.video.ffmpeg")
  String ffmpegPath;

  @Inject
  @ConfigProperty(name = "tech.lacambra.video.ffprobe")
  String ffprobePath;

  public void encode(String pathToConvert, String workingPath, String targetFileName) {

    LOGGER.info("[encode] Encoding file " + pathToConvert);

    FFmpeg ffmpeg = null;
    FFprobe ffprobe = null;
    FFmpegProbeResult fFmpegProbeResult;

    try {
      ffmpeg = new FFmpeg(ffmpegPath);
      ffprobe = new FFprobe(ffprobePath);
      fFmpegProbeResult = ffprobe.probe(pathToConvert);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    FFmpegBuilder builder = new FFmpegBuilder()
        .setVerbosity(FFmpegBuilder.Verbosity.WARNING)
        .setInput(fFmpegProbeResult)
        .overrideOutputFiles(true)
        .addOutput(workingPath + targetFileName)   // Filename for the destination
        .setFormat("mp4")
        .setVideoQuality(10)
        .setVideoCodec("libx264")     // Video using x264
        .setVideoFrameRate(24, 1)
        .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
        .done();

    FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
    executor.createJob(builder).run();

    LOGGER.info("[encode] Encoding done");
  }
}
