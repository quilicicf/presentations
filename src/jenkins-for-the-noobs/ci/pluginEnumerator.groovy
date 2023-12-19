import groovy.json.JsonBuilder

final String JENKINS_HOME = System.getenv("JENKINS_HOME")
final String JENKINS_VERSION = jenkins.model.Jenkins.getInstance().getVersion()

/**
 * Extract lines that contain a certain prefix from a Jenkins manifest file
 */
static def extractFromManifest (final List<String> manifestLines, final String prefix) {
  final String line = manifestLines
    .find { manifestLine -> manifestLine.startsWith(prefix) }
  return line.replace(prefix, "")
}

class PluginMetadata {
  public String groupId
  public String shortName
  public String version

  @Override
  String toString () {
    return "${groupId}:${shortName}:${version}"
  }
}

//noinspection GrDeprecatedAPIUsage for FileNameFinder. Couldn't find the right way to do this, change welcome.
final Map<String, String> installedPlugins = new FileNameFinder()
  .getFileNames("${JENKINS_HOME}/plugins", "**/META-INF/MANIFEST.MF")
  .collect { manifestPath ->
    final List<String> manifestLines = new File(manifestPath).readLines()
    final String groupId = extractFromManifest(manifestLines, "Group-Id: ")
    final String shortName = extractFromManifest(manifestLines, "Short-Name: ")
    final String version = extractFromManifest(manifestLines, "Plugin-Version: ")
    return new PluginMetadata(groupId: groupId, shortName: shortName, version: version)
  }
  .sort { a, b -> a.shortName <=> b.shortName } // Sort by short name
  .findAll { plugin -> !plugin.version.contains("private") } // Filter-out plugins installed from file
  .collectEntries { plugin -> [(plugin.shortName): plugin.toString()] }

println new JsonBuilder(jenkinsVersion: JENKINS_VERSION, plugins: installedPlugins).toPrettyString()
