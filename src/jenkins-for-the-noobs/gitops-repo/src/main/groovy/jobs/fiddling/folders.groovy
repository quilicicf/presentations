package jobs.fiddling

folder('__fiddling__') {
  displayName 'Fiddling'
  description '''\
    ## Fiddling folder

    Test your jobs here from the UI before putting them in CasC
  '''.stripIndent()
  icon {
    customFolderIcon {
      foldericon('fiddling.svg') // TODO: user content
    }
  }
}
