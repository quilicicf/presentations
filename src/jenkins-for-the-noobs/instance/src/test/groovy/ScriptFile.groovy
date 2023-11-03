import java.util.regex.Matcher
import java.util.regex.Pattern

class ScriptFile {
  final String fullPath
  final String displayName

  ScriptFile (final String fullPath, final String parentFolder) {
    this.fullPath = fullPath
    this.displayName = relativize(parentFolder, fullPath)
  }

  static String relativize (final String parentFolder, final String filePath) {
    final Matcher matcher = Pattern.compile(".*${parentFolder}/(?<relativePath>.*)")
      .matcher(filePath)
    matcher.find()
    return matcher.group('relativePath')
  }
}
