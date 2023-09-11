#!/usr/bin/env node

import versionChecker from './versionChecker.mjs';
import { FarjsApp } from '../farjs-app-opt.js';

process.title = "FAR.js";

var npmVersion = undefined;

const onReady = () => {
  versionChecker.fetchLatestVersion().then((version) => {
    npmVersion = version;
  }, (error) => {
    console.log(`Failed to fetch latest version from npm, error: ${error}`);
  });
};

const onExit = () => {
  versionChecker.checkNpmVersion(npmVersion);
  process.exit(0);
};

FarjsApp.start(false, onReady, onExit);
