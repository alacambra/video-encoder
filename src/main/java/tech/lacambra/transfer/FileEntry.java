package tech.lacambra.transfer;

import javax.json.Json;
import javax.json.JsonString;

public class FileEntry {

  private String name;
  private String fullPath;

  public FileEntry(String name, String fullPath) {
    this.name = name;
    this.fullPath = fullPath;
  }

  public JsonString getNameAsJson() {
    return Json.createValue(name);
  }
}
