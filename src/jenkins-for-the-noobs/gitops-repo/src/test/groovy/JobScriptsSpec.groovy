import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.plugin.JenkinsJobManagement
import org.junit.ClassRule
import org.jvnet.hudson.test.JenkinsRule
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.charset.StandardCharsets.UTF_8

/**
 * This class describes how the Job DSL scripts are processed before being fed to the
 * test method provided the Job DSL plugin.
 * <br>
 * It is specifically crafted for instances that use pure groovy files as source for Job
 * DSL scripts and cannot work on other instances.
 */
class JobScriptsSpec extends Specification {
  @Shared
  @ClassRule
  JenkinsRule jenkinsRule = new JenkinsRule()

  private static final String BASE_DIR = 'src/main/groovy/jobs'
  private static final String FOLDERS_DECLARATION_FILE_NAME = 'folders.groovy'

  /**
   * Processes the files in the correct order so that no item is processed before
   * its containing folder. Each source code folder is supposed to contain a file
   * named {@link JobScriptsSpec#FOLDERS_DECLARATION_FILE_NAME} with the folders,
   * then other scripts with the jobs.
   * <br>
   * The sorting algorithm is:
   * <ul>
   *   <li>By depth first</li>
   *   <li>Then by folder name's alphabetical order</li>
   *   <li>Then {@link JobScriptsSpec#FOLDERS_DECLARATION_FILE_NAME} first</li>
   *   <li>Then by file's alphabetical order</li>
   * </ul>
   */
  private static final Comparator<String> PATHS_COMPARATOR = new Comparator<String>() {
    @Override
    int compare (final String a, final String b) {
      final int depthComparison = a.split('/').length <=> b.split('/').length
      final boolean isAFoldersFile = isFoldersFile(a)
      final boolean isBFoldersFile = isFoldersFile(b)
      final String aFolder = getFolderName(a)
      final String bFolder = getFolderName(b)

      if (depthComparison != 0) {
        return depthComparison
      } else if (aFolder != bFolder) {
        return aFolder <=> bFolder
      } else if (isAFoldersFile && !isBFoldersFile) {
        return -1
      } else if (isBFoldersFile && !isAFoldersFile) {
        return 1
      } else {
        return a <=> b
      }
    }
  }

  private static String getFolderName (final String path) {
    return Paths.get(path).getParent()
  }

  private static boolean isFoldersFile (final String path) {
    return path.endsWith("/${FOLDERS_DECLARATION_FILE_NAME}")
  }

  private static String toDisplayablePath (final String path) {
    return path.replaceAll(".*/${BASE_DIR}/", '')
  }

  /**
   * Reads all the files that contain Job DSL scripts in <code>src/main/groovy/jobs</code>.
   */
  static List<ScriptFile> loadJobDslFiles () {
    println 'Files processed in following order:'
    //noinspection GrDeprecatedAPIUsage
    return new FileNameFinder()
      .getFileNames(BASE_DIR, '**/*.groovy')
      .sort { a, b -> PATHS_COMPARATOR.compare(a, b) }
      .each { println " * ${toDisplayablePath(it)}" }
      .collect { it -> new ScriptFile(it, BASE_DIR) }
  }

  @Unroll('#jobsScriptFile.displayName')
  void test (final ScriptFile jobsScriptFile) {
    given:
    //noinspection GroovyAssignabilityCheck Do not try to change def to JenkinsJobManagement, doesn't compile, god knows why
    final def jobManagement = new JenkinsJobManagement(System.out, [:], new File('.'))

    when:
    final Path file = Paths.get(jobsScriptFile.fullPath)
    final String dslScript = new String(Files.readAllBytes(file), UTF_8)
    println "Loading job DSL script ${jobsScriptFile.displayName}:"
    new DslScriptLoader(jobManagement)
      .runScript(dslScript)

    then:
    noExceptionThrown()

    where:
    jobsScriptFile << loadJobDslFiles()
  }
}
