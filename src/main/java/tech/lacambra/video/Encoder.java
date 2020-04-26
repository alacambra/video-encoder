package tech.lacambra.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import javax.enterprise.context.Dependent;
import java.io.IOException;

@Dependent
public class Encoder {

  public void encode() {

    FFmpeg ffmpeg = null;
    FFprobe ffprobe = null;
    FFmpegProbeResult fFmpegProbeResult;

    try {
      ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
      ffprobe = new FFprobe("/usr/local/bin/ffprobe");
      fFmpegProbeResult = ffprobe.probe("/Users/albertlacambra/Desktop/aws-videos/test.mov");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    FFmpegBuilder builder = new FFmpegBuilder()

        .setInput(fFmpegProbeResult)     // Filename, or a FFmpegProbeResult
//        .setInput("/Users/albertlacambra/Desktop/aws-videos/test.mov")     // Filename, or a FFmpegProbeResult
        .overrideOutputFiles(true) // Override the output if it exists

        .addOutput(String.format("output.%s.mp4", System.currentTimeMillis()))   // Filename for the destination
        .setFormat("mp4")        // Format is inferred from filename, or can be set
//        .setTargetSize(250_000l)  // Aim for a 250KB file
        .setTargetSize(100_000)
        .disableSubtitle()       // No subtiles

//        .setAudioChannels(1)         // Mono audio
//        .setAudioCodec("aac")        // using the aac codec
//        .setAudioSampleRate(48_000)  // at 48KHz
//        .setAudioBitRate(32768)      // at 32 kbit/s

        .setVideoCodec("libx264")     // Video using x264
        .setVideoFrameRate(24, 1)     // at 24 frames per second
//        .setVideoResolution(640, 480) // at 640x480 resolution

        .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
        .done();

    FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

// Run a one-pass encode
    executor.createJob(builder).run();

// Or run a two-pass encode (which is better quality at the cost of being slower)
    executor.createTwoPassJob(builder).run();
  }
}
