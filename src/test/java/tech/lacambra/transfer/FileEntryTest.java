package tech.lacambra.transfer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileEntryTest {

  @Test
  void testSize() {
    FileEntry fileEntry = new FileEntry("", "-rwxrwxrwx    1 alacambra users    13434869 May  1 23:33 ScreenRecording2020-05-01at23.33.45-1.mov");
    assertEquals(fileEntry.extractSize(), 13434869);
  }
}