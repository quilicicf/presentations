import com.cloudbees.plugins.credentials.Credentials
import com.cloudbees.plugins.credentials.CredentialsProvider
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.security.AuthorizationStrategy
import hudson.security.SecurityRealm
import io.jenkins.plugins.casc.ConfigurationAsCode
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.jenkinsci.plugins.GithubAuthorizationStrategy
import org.jenkinsci.plugins.GithubSecurityRealm
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

  private final static String ADMIN_USER = System.getenv("ADMIN_USER") ?: 'quilicicf'

  /**
   * Sets Jenkins instance port, see how localPort is set in {@link JenkinsRule}
   */
  static {
    System.setProperty('port', '8201')
    System.setProperty('org.jenkinsci.plugins.durabletask.BourneShellScript.LAUNCH_DIAGNOSTICS', 'true')
  }

  @Shared
  @ClassRule
  JenkinsRule jenkinsRule = new JenkinsRule()

  /** Keep server open for a long time, default timeout is 180s, which is too low to present the slides */
  @Timeout(value = 1L, unit = TimeUnit.DAYS)
  /** Only run with run configuration TEST_INSTANCE, not with gradle test */
  @IgnoreIf({ System.getenv('CASC_VAULT_URL') == null })
  void test () {
    when:
    cascConfiguration()
    jobDslConfiguration()
    authenticationConfiguration()
    authorizationConfiguration(ADMIN_USER)

    println '===================================='
    println 'INSTANCE READY TO BE INTERACTED WITH'
    Thread.currentThread().join() // Stop current thread to keep Jenkins instance alive while playing with it

    then:
    noExceptionThrown()
  }

  void cascConfiguration () {
    loadConfigFiles()
      .forEach {
        println "Loading configuration file ${it.displayName}:"
        ConfigurationAsCode.get().configure(it.fullPath)
      }
  }

  void jobDslConfiguration () {
    final def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))
    loadJobDslFiles()
      .forEach {
        final Path file = Paths.get(it.fullPath)
        final String dslScript = new String(Files.readAllBytes(file), UTF_8)
        println "Loading job DSL script ${it.displayName}:"
        new DslScriptLoader(jobManagement)
          .runScript(dslScript)
      }
  }

  /**
   * Instructions come from the <a href="https://plugins.jenkins.io/github-oauth">plugin page</a>.
   */
  void authenticationConfiguration () {
    final String githubWebUri = 'https://github.com'
    final String githubApiUri = 'https://api.github.com'
    final String oauthScopes = 'read:org,user:email'

    final UsernamePasswordCredentialsImpl credential = CredentialsProvider
      .lookupCredentials(Credentials.class, jenkinsRule.instance, null, null)
      .findAll { it instanceof UsernamePasswordCredentials }
      .collect { (UsernamePasswordCredentialsImpl) it }
      .find { it.getId() == 'github-authentication-app' }

    final SecurityRealm githubRealm = new GithubSecurityRealm(
      githubWebUri, githubApiUri,
      credential.username, credential.password.getPlainText(),
      oauthScopes
    )
    if (githubRealm != jenkinsRule.instance.getSecurityRealm()) {
      jenkinsRule.instance.setSecurityRealm(githubRealm)
      jenkinsRule.instance.save()
    }
  }

  /**
   * Instructions come from the <a href="https://plugins.jenkins.io/github-oauth">plugin page</a>.
   */
  void authorizationConfiguration (final String adminUserNames) {
    final String organizationNames = ''
    final boolean useRepositoryPermissions = true
    final boolean authenticatedUserReadPermission = false
    final boolean authenticatedUserCreateJobPermission = false
    final boolean allowGithubWebHookPermission = false
    final boolean allowCcTrayPermission = false
    final boolean allowAnonymousReadPermission = false
    final boolean allowAnonymousJobStatusPermission = false

    final AuthorizationStrategy github_authorization = new GithubAuthorizationStrategy(
      adminUserNames,
      authenticatedUserReadPermission,
      useRepositoryPermissions,
      authenticatedUserCreateJobPermission,
      organizationNames,
      allowGithubWebHookPermission,
      allowCcTrayPermission,
      allowAnonymousReadPermission,
      allowAnonymousJobStatusPermission
    )

    if (github_authorization != jenkinsRule.instance.getAuthorizationStrategy()) {
      jenkinsRule.instance.setAuthorizationStrategy(github_authorization)
      jenkinsRule.instance.save()
    }
  }
}
