#!/usr/bin/env node

import versionChecker from './versionChecker.mjs';
import { FarjsApp } from '../farjs-app-opt.js';

process.title = "FAR.js";

var npmVersion = undefined;
versionChecker.fetchLatestVersion().then((version) => {
  npmVersion = version;
});

const onExit = () => {
  versionChecker.checkNpmVersion(npmVersion);
  process.exit(0);
};

FarjsApp.start(false, undefined, onExit);
