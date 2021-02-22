#!/usr/bin/env node

const versionChecker = require('../dist/versionChecker.js')
versionChecker.check(() => {

  const { FarjsApp } = require('../dist/far.js')

  FarjsApp.start()
})
