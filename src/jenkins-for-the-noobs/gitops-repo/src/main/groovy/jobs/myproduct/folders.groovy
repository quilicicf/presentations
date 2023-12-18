package jobs.myproduct

folder('myproduct') {
  displayName 'My product'
  description '''\
    ## My awesome product

    Contains all jobs related to blabla...
  '''.stripIndent()
  icon {
    customFolderIcon {
      foldericon('product_logo.svg') // TODO: user content
    }
  }
}

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
