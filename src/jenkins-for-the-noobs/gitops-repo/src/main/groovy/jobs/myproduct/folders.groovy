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
