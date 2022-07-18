#!/usr/bin/env node

const versionChecker = require('../dist/versionChecker.js')
versionChecker.check(() => {

  require('../farjs-app-opt-bundle.js')
})
