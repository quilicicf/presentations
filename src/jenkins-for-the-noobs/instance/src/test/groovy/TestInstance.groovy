import io.jenkins.plugins.casc.ConfigurationAsCode
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.junit.ClassRule
import org.junit.jupiter.api.Timeout
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static ConfigScriptsSpec.loadConfigFiles
import static JobScriptsSpec.loadJobDslFiles
import static java.nio.charset.StandardCharsets.UTF_8

class TestInstance extends Specification {

  /**
   * Sets Jenkins instance port, see how localPort is set in {@link JenkinsRule}
   */
  static { System.setProperty('port', '8201') }

  @Shared
  @ClassRule
  JenkinsRule jenkinsRule = new JenkinsRule()

  /** Keep server open for a long time, default timeout is 180s, which is too low to present the slides */
  @Timeout(value = 1L, unit = TimeUnit.DAYS)
  /** Only run with run configuration TEST_INSTANCE, not with gradle test */
  @IgnoreIf({ System.getenv('CASC_VAULT_URL') == null })
  void test () {
    given:
    final def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))

    when:
    loadJobDslFiles()
      .forEach {
        final Path file = Paths.get(it.fullPath)
        final String dslScript = new String(Files.readAllBytes(file), UTF_8)
        println "Loading job DSL script ${it.displayName}:"
        new DslScriptLoader(jobManagement)
          .runScript(dslScript)
      }

    loadConfigFiles()
      .forEach {
        println "Loading configuration file ${it.displayName}:"
        ConfigurationAsCode.get().configure(it.fullPath)
      }

    println '===================================='
    println 'INSTANCE READY TO BE INTERACTED WITH'
    Thread.currentThread().join() // Stop current thread to keep Jenkins instance alive while playing with it

    then:
    noExceptionThrown()
  }
}
