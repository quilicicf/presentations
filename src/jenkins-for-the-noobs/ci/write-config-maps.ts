import type { WalkEntry } from 'fs/walk.ts';
import { walk, WalkOptions } from 'fs/walk.ts';
import { join } from 'path/join.ts';

import {
  buildJenkinsInstance,
  computeTargetJobsFilePath,
  JenkinsInstance,
  JOBS_FOLDERS_FILE_NAME,
} from './lib/jenkinsInstance.ts';
import { CYAN, log, LoggerOptions } from './lib/log.ts';
import { findRepositoryPath } from './lib/git.ts';
import { buildCascConfigMap, buildJobsConfigMap } from './lib/kubernetesConfigMapTemplate.ts';

async function main (): Promise<void> {
  const isVerbose: boolean = Deno.args.includes('--verbose');
  const repositoryPath: string = await findRepositoryPath();
  const jenkinsInstance: JenkinsInstance = buildJenkinsInstance('jenkins-myteam', repositoryPath);

  await writeKubernetesFiles(jenkinsInstance, { isVerbose, indentation: '  ', baseLevel: 0 });
}

async function writeKubernetesFiles (jenkinsInstance: JenkinsInstance, loggerOptions: LoggerOptions): Promise<void> {
  log(`Processing Jenkins instance`, loggerOptions, { alwaysLog: true, styles: [] });

  await Deno.mkdir(jenkinsInstance.generatedFolderPath, { recursive: true });

  const jobFolders: WalkEntry[] = await findChildFolders(jenkinsInstance.jobsSourceFolderPath, 1);
  await Promise.all(
    jobFolders.map((jobFolder: WalkEntry) => writeJobsFileForFolder(jobFolder, jenkinsInstance, loggerOptions)),
  );

  const cascFiles: WalkEntry[] = await findChildFiles(jenkinsInstance.cascSourceFolderPath, Infinity);
  await writeCascFile(cascFiles, jenkinsInstance, loggerOptions);
}

async function writeJobsFileForFolder (jobFolder: WalkEntry, jenkinsInstance: JenkinsInstance, loggerOptions: LoggerOptions): Promise<void> {
  const jobFiles: WalkEntry[] = await findChildFiles(jobFolder.path, Infinity);
  const scripts: string[] = await Promise.all(
    jobFiles
      .filter((file: WalkEntry) => file.name.endsWith('.groovy'))
      .sort((left, right) => sortJobFiles(left, right))
      .map((jobFile: WalkEntry) => {
        log(
          `* Processing Job DSL script %c${jobFolder.name}/${jobFile.name}`,
          loggerOptions,
          { relativeLevel: 1, styles: [ CYAN ] },
        );
        return jobFile;
      })
      .map((jobFile: WalkEntry) => ({
        relativePath: join(jobFolder.name, jobFile.name),
        absolutePath: jobFile.path,
      }))
      .map(async ({ relativePath, absolutePath }) => (
        `// Source: ${relativePath}\n${await Deno.readTextFile(absolutePath)}`
      )),
  );

  const configMapPath: string = computeTargetJobsFilePath(jenkinsInstance, jobFolder.name);
  const targetFileContent: string = buildJobsConfigMap(jenkinsInstance.name, jobFolder.name, scripts);

  await Deno.writeTextFile(configMapPath, targetFileContent);
}

async function writeCascFile (cascFiles: WalkEntry[], jenkinsInstance: JenkinsInstance, loggerOptions: LoggerOptions): Promise<void> {
  const configs: string[] = await Promise.all(
    cascFiles
      .filter((file: WalkEntry) => file.name.endsWith('.yaml'))
      .sort((left, right) => sortFilesByName(left, right))
      .map((jobFile: WalkEntry) => {
        log(
          `* Processing CasC file %c${jobFile.name}`,
          loggerOptions,
          { relativeLevel: 1, styles: [ CYAN ] },
        );
        return jobFile;
      })
      .map(async (cascFile: WalkEntry) => await Deno.readTextFile(cascFile.path)),
  );

  const targetFileContent: string = buildCascConfigMap(jenkinsInstance.name, configs);
  await Deno.writeTextFile(jenkinsInstance.cascTargetPath, targetFileContent);
}

function sortJobFiles (left: WalkEntry, right: WalkEntry): number {
  if (left.path === right.path) {
    return 0;
  }

  const leftPathLength = left.path.split('/').length;
  const rightPathLength = right.path.split('/').length;
  if (leftPathLength !== rightPathLength) {
    return leftPathLength < rightPathLength ? -1 : 1;
  }

  if (left.name === JOBS_FOLDERS_FILE_NAME) {
    return -1;
  }

  if (right.name === JOBS_FOLDERS_FILE_NAME) {
    return 1;
  }

  return sortFilesByName(left, right);
}

function sortFilesByName (left: WalkEntry, right: WalkEntry): number {
  if (left.name === right.name) {
    return 0;
  }
  return left.name < right.name ? -1 : 1;
}

async function findChildFolders (parentFolderPath: string, maxDepth: number) {
  return await findChildren(parentFolderPath, { maxDepth, includeDirs: true, includeFiles: false });
}

async function findChildFiles (parentFolderPath: string, maxDepth: number) {
  return await findChildren(parentFolderPath, { maxDepth, includeDirs: false, includeFiles: true });
}

async function findChildren (parentFolderPath: string, options: WalkOptions): Promise<WalkEntry[]> {
  const iterator: AsyncIterable<WalkEntry> = walk(parentFolderPath, options);
  return (await iteratorToArray(iterator))
    .filter((entry: WalkEntry) => entry.path !== parentFolderPath);
}

async function iteratorToArray<T> (iterator: AsyncIterable<T>): Promise<T[]> {
  const array: T[] = [];
  for await(const item of iterator) {
    array.push(item);
  }
  return array;
}

main()
  .catch((error) => {
    console.error(`Script failed with:\n${error.stack}`);
    Deno.exit(1);
  });
