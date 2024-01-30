import io.jenkins.plugins.casc.ConfigurationAsCode
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * See https://github.com/jenkinsci/configuration-as-code-plugin/blob/master/docs/PLUGINS.md#how-to-test
 */
class ConfigScriptsSpec extends Specification {
  @Shared
  @ClassRule
  JenkinsRule jenkinsRule = new JenkinsRule()

  private static final String BASE_DIR = 'src/main/resources/casc'

  /**
   * Reads all the files that contain CasC scripts in <code>src/main/resources/casc</code>.
   */
  static List<ScriptFile> loadConfigFiles () {
    //noinspection GrDeprecatedAPIUsage
    return new FileNameFinder()
      .getFileNames(BASE_DIR, "**/*.yaml")
      .sort { a, b -> a.split('/').length <=> b.split('/').length }
      .collect { new ScriptFile(it, BASE_DIR) }
  }

  @Unroll('#file.displayName')
  void test (final ScriptFile file) {
    when:
    ConfigurationAsCode.get().configure(file.fullPath)
    then:
    noExceptionThrown()
    where:
    file << loadConfigFiles()
  }
}
