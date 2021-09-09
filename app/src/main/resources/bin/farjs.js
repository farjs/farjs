#!/usr/bin/env node

const versionChecker = require('../dist/versionChecker.js')
versionChecker.check(() => {

  require('../dist/far.js')
})
