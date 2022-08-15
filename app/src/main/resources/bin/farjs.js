#!/usr/bin/env node

process.title = "FAR.js"

const versionChecker = require('../dist/versionChecker.js')
versionChecker.check(() => {

  require('../farjs-app-opt-bundle.js').FarjsApp.start()
})
