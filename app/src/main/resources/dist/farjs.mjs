#!/usr/bin/env node

import { FarjsApp } from '../farjs-app-opt.js';

process.title = "FAR.js";

var npmVersion = undefined;

const onReady = () => {
  import('./versionChecker.mjs').then((versionChecker) => {
    versionChecker.fetchLatestVersion().then((version) => {
      npmVersion = version;
      console.log(`Latest version: ${version}`);
    }, (error) => {
      console.log(`Failed to fetch latest version from npm, error: ${error}`);
    });
  });
};

const onExit = () => {
  import('./versionChecker.mjs').then((versionChecker) => {
    versionChecker.checkNpmVersion(npmVersion);
    process.exit(0);
  });
};

FarjsApp.start(false, onReady, onExit);
