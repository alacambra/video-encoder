package tech.lacambra.transfer;

import javax.json.Json;
import javax.json.JsonString;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileEntry {

  private static final Pattern PATTERN = Pattern.compile("users[ ]+(\\d+) ");
  private String name;
  private String fullPath;

  public FileEntry(String name, String fullPath) {
    this.name = name;
    this.fullPath = fullPath;
  }

  public JsonString getName() {
    return Json.createValue(name);
  }

  public JsonString getSize() {

    BigDecimal size = new BigDecimal(extractSize())
        .divide(new BigDecimal(1024 * 1024), 2, RoundingMode.HALF_EVEN);
    return Json.createValue(size + "MB");
  }

  int extractSize() {

    Matcher matcher = PATTERN.matcher(fullPath);

    if (matcher.find()) {

      int start = matcher.start(1);
      int end = matcher.end(1);

      return Integer.parseInt(fullPath.substring(start, end));

    }

    return -1;
  }
}
