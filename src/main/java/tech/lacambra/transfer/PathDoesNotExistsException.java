package tech.lacambra.transfer;

public class PathDoesNotExistsException extends RuntimeException {

  private PathDoesNotExistsException() {
  }

  private PathDoesNotExistsException(String path) {
    super("Path %s not found");
  }

  public static PathDoesNotExistsException fileDoesNotExists(String file) {
    return new PathDoesNotExistsException(String.format("File %s not found", file));
  }

  public static PathDoesNotExistsException folderDoesNotExists(String folder) {
    return new PathDoesNotExistsException(String.format("Folder %s not found", folder));
  }

  public static PathDoesNotExistsException pathDoesNotExists(String path) {
    return new PathDoesNotExistsException(String.format("Path %s not found", path));
  }
}
