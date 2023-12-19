package jobs.jenkins

folder('jenkins') {
  displayName 'Jenkins jobs'
  description '''\
    ## CI-ception

    Contains jobs that validate the current Jenkins instance.
    
    It's magic.
  '''.stripIndent()
  icon {
    customFolderIcon {
      foldericon('jenkins_logo.svg') // TODO: user content
    }
  }
}
