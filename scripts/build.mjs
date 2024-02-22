#!/usr/bin/env node

import { resolve } from 'path';
import { copyFileSync, readdirSync, readFileSync, writeFileSync } from 'fs';
import { exec } from 'child_process';

const SCRIPT_PATH = new URL(import.meta.url).pathname;
const APP_ROOT_FOLDER = resolve(SCRIPT_PATH, '..', '..');
const SRC_FOLDER = resolve(APP_ROOT_FOLDER, 'src');
const DIST_FOLDER = resolve(APP_ROOT_FOLDER, 'dist');

const COLORS = {
  CYAN: '\x1b[36m',
  DEFAULT: '\x1b[0m',
};

async function main () {
  const deckMetadataList = [ ...readdirSync(SRC_FOLDER, { withFileTypes: true }) ]
    .filter((entry) => entry.isDirectory())
    .map((entry) => readDeckMetadata(entry.name));

  generateIndexPage(deckMetadataList);
  generatePresentations(deckMetadataList);
}

function generatePresentations (deckMetadataList) {
  deckMetadataList
    .map((deckMetadata) => generatePresentation(deckMetadata));
}

async function generatePresentation (deckMetadata) {
  process.stdout.write(`Processing ${COLORS.CYAN}${deckMetadata.id}${COLORS.DEFAULT}\n`);
  copyFileSync(
    deckMetadata.faviconMoveInfo.faviconInputFilePath,
    deckMetadata.faviconMoveInfo.faviconOutputFilePath,
  );
  return new Promise((resolve, reject) => {
    const childProcess = exec(
      `a2r build --input-file ${deckMetadata.fullInputPath} --output-file ${deckMetadata.fullOutputPath}`,
      function callback (error) {
        if (error) {
          reject(Error(`Processing failed: ${error.stack}`));
        } else {
          resolve();
        }
      },
    );
    childProcess.stdout.pipe(process.stdout);
    childProcess.stderr.pipe(process.stderr);
  });
}

function generateIndexPage (deckMetadataList) {
  const tilesToInsert = deckMetadataList
    .map((deckMetadata) => toTile(deckMetadata))
    .join('\n');

  const sourceHtmlPath = resolve(SRC_FOLDER, 'index.html');
  const targetHtmlPath = resolve(DIST_FOLDER, 'index.html');
  const inputHtml = readFileSync(sourceHtmlPath, 'utf8');
  const updatedHtml = substituteTilesInFile(inputHtml, tilesToInsert);
  writeFileSync(targetHtmlPath, updatedHtml, 'utf8');

  const sourceCssPath = resolve(SRC_FOLDER, 'index.css');
  const targetCssPath = resolve(DIST_FOLDER, 'index.css');
  copyFileSync(sourceCssPath, targetCssPath);
}

function readDeckMetadata (deckFolderName) {
  process.stdout.write(`Reading ${COLORS.CYAN}${deckFolderName}${COLORS.DEFAULT}\n`);
  const deckName = `${deckFolderName}.adoc`;
  const deckContent = readFileSync(resolve(SRC_FOLDER, deckFolderName, deckName), 'utf8');
  const [ , title ] = /^:a2r-page-title: ([^\n]+)$/gm.exec(deckContent);
  const [ , favicon ] = /^:a2r-favicon: ([^\n]+)$/gm.exec(deckContent);
  const [ , faviconExtension ] = /\.([^.]+)$/.exec(favicon);

  const outputFaviconName = `${deckFolderName}-favicon.${faviconExtension}`;
  const faviconInputFilePath = resolve(SRC_FOLDER, deckFolderName, favicon);
  const faviconOutputFilePath = resolve(DIST_FOLDER, outputFaviconName);

  return {
    id: deckFolderName,
    title,
    fullInputPath: resolve(SRC_FOLDER, deckFolderName, deckName),
    fullOutputPath: resolve(DIST_FOLDER, deckName.replaceAll(/\.adoc$/g, '.html')),
    faviconMoveInfo: {
      faviconInputFilePath,
      faviconOutputFilePath,
    },
    indexLink: {
      path: `./${deckFolderName}.html`,
      faviconPath: `./${outputFaviconName}`,
    },
  };
}

function toTile (deckMetadata) {
  return `  <a href="${deckMetadata.indexLink.path}" class="tile">
    <img src="${deckMetadata.indexLink.faviconPath}" alt="Presentation favicon" /><span>${deckMetadata.title}</span>
  </a>`;
}

function substituteTilesInFile (fileContent, tiles) {
  const startMark = '<!-- START: tiles -->';
  const endMark = '<!-- END:   tiles -->';
  const regex = new RegExp(`${startMark}.*?${endMark}`, 'gs');
  return fileContent.replace(regex, `${startMark}\n${tiles}\n  ${endMark}`);
}

main()
  .catch((error) => console.error(`Failed with error: ${error.stack}\n`));
