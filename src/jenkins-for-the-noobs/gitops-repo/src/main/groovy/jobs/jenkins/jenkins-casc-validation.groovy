package jobs.jenkins

multibranchPipelineJob('jenkins/validate') {
  displayName 'Validate Jenkins CasC'
  description 'Job that runs validation of CasC on this very Jenkins instance.'
  branchSources {
    branchSource {
      source {
        github {
          id 'f3fa5ecc-f606-4fa4-aee8-680076a9901b'
          repoOwner 'quilicicf'
          repository 'presentations'
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
              strategyId 3 // All branches
            }
            notificationContextTrait {
              typeSuffix false
              contextLabel 'Validate CasC'
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
      scriptPath 'src/jenkins-for-the-noobs/gitops-repo/Jenkinsfile'
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
      foldericon('build_and_release.svg') // TODO: user content
    }
  }
}
