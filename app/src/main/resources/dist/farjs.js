#!/usr/bin/env node

process.title = "FAR.js"

const versionChecker = require('./versionChecker.js')

var npmVersion = undefined
versionChecker.fetchLatestVersion().then((version) => {
  npmVersion = version
})

const onExit = () => {
  versionChecker.checkNpmVersion(npmVersion)
  process.exit(0)
}

const farjsApp = require('../farjs-app-opt.js').FarjsApp
farjsApp.start(false, undefined, onExit)
