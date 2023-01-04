#!/usr/bin/env node

const moduleAlias = require('module-alias')
moduleAlias.addAliases({
  'react-redux': 'react-redux/lib/alternate-renderers'
})

process.title = "FAR.js"

const versionChecker = require('./versionChecker.js')

var npmVersion = undefined
versionChecker.getNpmVersion((error, version) => {
  if (!error) {
    npmVersion = version
  }
})

const onExit = () => {
  versionChecker.checkNpmVersion(npmVersion)
  process.exit(0)
}

const farjsApp = require('../farjs-app-opt.js').FarjsApp
farjsApp.start(false, undefined, onExit)
