import com.cloudbees.plugins.credentials.SystemCredentialsProvider
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl
import hudson.model.User
import hudson.security.AuthorizationStrategy
import hudson.security.SecurityRealm
import io.jenkins.plugins.casc.ConfigurationAsCode
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import jenkins.security.ApiTokenProperty
import jenkins.security.apitoken.TokenUuidAndPlainValue
import org.jenkinsci.plugins.GithubAuthorizationStrategy
import org.jenkinsci.plugins.GithubSecurityRealm
import org.junit.ClassRule
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.recipes.WithTimeout
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static ConfigScriptsSpec.loadConfigFiles
import static JobScriptsSpec.loadJobDslFiles
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentialsInItemGroup
import static java.lang.Integer.MAX_VALUE
import static java.nio.charset.StandardCharsets.UTF_8
import static java.util.Collections.emptyMap

class TestInstance extends Specification {

  private static final String JENKINS_PORT = '8201'
  private static final String ADMIN_USER = System.getenv("ADMIN_USER") ?: 'quilicicf'

  /** Sets Jenkins instance port, see how localPort is set in {@link JenkinsRule} */
  static {
    println "Jenkins will start on: http://localhost:${JENKINS_PORT}/jenkins"
    System.setProperty('port', JENKINS_PORT)
  }

  /** Add debugging logs for Docker agents */
  static {
    System.setProperty('org.jenkinsci.plugins.durabletask.BourneShellScript.LAUNCH_DIAGNOSTICS', 'true')
  }

  /** Change Jenkins folder for permissions issues */
  // FIXME: check this fix, doesn't seem to work well
  static {
    final String jenkinsFolder = Paths.get(System.getenv('HOME'))
      .resolve('.jenkins_home')
      .toAbsolutePath()
      .toString()
    println "Setting Jenkins folder to ${jenkinsFolder}"
    System.setProperty('java.io.tmpdir', jenkinsFolder)

    println "CASC_VAULT_URL=${System.getenv('CASC_VAULT_URL')}"
  }

  @Rule
  JenkinsRule jenkinsRule = new JenkinsRule()

  /** Keep server open for infinity and beyond. Default timeout is 180s, which is too low to present the slides */
  @WithTimeout(MAX_VALUE)
  /** Only run with run configuration TEST_INSTANCE, not with gradle test */
  @IgnoreIf({ System.getenv('CASC_VAULT_URL') == null })
  void test () {
    when:
    cascConfiguration()
    jobDslConfiguration()
    authenticationConfiguration()
    authorizationConfiguration(ADMIN_USER)
    createUserToken(ADMIN_USER)

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

    final UsernamePasswordCredentials credential = lookupCredentialsInItemGroup(UsernamePasswordCredentials.class, jenkinsRule.jenkins, null)
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

  void createUserToken (final String userName) {
    final User user = User.get(userName, true, emptyMap())
    final ApiTokenProperty apiTokenProperty = user.getProperty(ApiTokenProperty.class)
    final TokenUuidAndPlainValue result = apiTokenProperty.tokenStore.generateNewToken('TEST')
    user.save()

    final UsernamePasswordCredentialsImpl jenkinsCredentials = lookupCredentialsInItemGroup(UsernamePasswordCredentialsImpl.class, jenkinsRule.jenkins, null)
      .find { it.id == 'jenkins-token' }

    final UsernamePasswordCredentials updatedCredentials = new UsernamePasswordCredentialsImpl(
      jenkinsCredentials.scope, jenkinsCredentials.id, jenkinsCredentials.description, userName, result.plainValue
    )

    SystemCredentialsProvider.instance
      .getStore()
      .updateCredentials(Domain.global(), jenkinsCredentials, updatedCredentials)
  }
}
