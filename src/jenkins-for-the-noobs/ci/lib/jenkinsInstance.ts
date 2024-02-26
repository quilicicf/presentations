import { resolve } from 'path/mod.ts';

const GENERATED_FOLDER_NAME: string = 'generated';
const INSTANCE_SUB_PATH: string = 'src/jenkins-for-the-noobs/gitops-repo';
const JOBS_SOURCE_SUB_PATH: string = 'src/main/groovy/jobs';
const CASC_SOURCE_SUB_PATH: string = 'src/main/resources/casc';
const CASC_TARGET_FILE_NAME: string = 'jenkins.yaml';

export const JOBS_FOLDERS_FILE_NAME: string = 'folders.groovy';

export interface JenkinsInstance {
  name: string,
  repositoryPath: string,

  path: string,
  generatedFolderPath: string,

  jobsSourceFolderPath: string,
  jobsTargetFolderPath: string,

  cascSourceFolderPath: string,
  cascTargetPath: string,
}

export function buildJenkinsInstance (instanceName: string, repositoryPath: string): JenkinsInstance {
  const instancePath: string = resolve(repositoryPath, INSTANCE_SUB_PATH);
  const generatedFolderPath: string = resolve(instancePath, GENERATED_FOLDER_NAME);
  const jobsSourceFolderPath = resolve(instancePath, JOBS_SOURCE_SUB_PATH);
  const cascSourceFolderPath = resolve(instancePath, CASC_SOURCE_SUB_PATH);
  return {
    name: instanceName,
    repositoryPath,
    path: instancePath,
    generatedFolderPath,

    jobsSourceFolderPath,
    jobsTargetFolderPath: resolve(instancePath, GENERATED_FOLDER_NAME),

    cascSourceFolderPath: cascSourceFolderPath,
    cascTargetPath: resolve(instancePath, GENERATED_FOLDER_NAME, CASC_TARGET_FILE_NAME),
  };
}

export function computeTargetJobsFilePath (jenkinsInstance: JenkinsInstance, folderName: string): string {
  return resolve(jenkinsInstance.jobsTargetFolderPath, `jobs-${folderName}.yaml`);
}
