package jobs.myproduct

multibranchPipelineJob('myproduct/product-repo') {
  displayName 'Build & release'
  description 'Build job for my product'
  branchSources {
    branchSource {
      source {
        github {
          id '409d8f70-c612-4c5d-9a73-5aa1d7484830'
          repoOwner 'MyOrg'
          repository 'product-repo'
          repositoryUrl ''
          configuredByUrl false
          credentialsId 'github-credentials'
          traits {
            localBranchTrait()
            pruneStaleBranchTrait()
            cleanAfterCheckoutTrait { extension { deleteUntrackedNestedRepositories(true) } }
            cleanBeforeCheckoutTrait { extension { deleteUntrackedNestedRepositories(true) } }
            gitHubIgnoreDraftPullRequestFilter()
            gitHubBranchDiscovery {
              strategyId 1 // Exclude branches that are also filed as PRs
            }
            gitHubPullRequestDiscovery {
              strategyId 2 // The current pull request revision
            }
            headRegexFilter {
              regex '^((?!__).)*$' // Ignore version branches (they contain `__`)
            }
            multiBranchProjectDisplayNaming {
              displayNamingStrategy 'RAW_AND_OBJECT_DISPLAY_NAME'
            }
            notificationContextTrait {
              typeSuffix false
              contextLabel 'Build & release'
            }
            cloneOptionTrait {
              extension {
                depth 1000 // Reasonable tradeoff to avoid pulling too much history or missing commits
                timeout 5
                noTags true
                shallow true
                reference ''
                honorRefspec true
              }
            }
          }
        }
      }
    }
  }
  triggers {
    periodicFolderTrigger {
      interval '1d' // Refresh folder every day to cleanup old builds
    }
  }
  factory {
    workflowBranchProjectFactory {
      scriptPath 'Jenkinsfile'
    }
  }
  orphanedItemStrategy {
    discardOldItems {
      numToKeep 5
      daysToKeep 14
    }
  }
  icon {
    customFolderIcon {
      foldericon('build_and_release.svg')
    }
  }
}
