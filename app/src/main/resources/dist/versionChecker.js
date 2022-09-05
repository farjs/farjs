"use strict";

const exec = require('child_process').exec
const packageJson = require('../package.json')

function getNpmVersion(callback) {
  const cmd = 'npm view ' + packageJson.name + ' version'
  exec(cmd, {
    windowsHide: true
  }, (error, stdout) => {
    if (error) {
      //console.error("Npm version check error: " + error)
      callback(true, false)
      return
    }

    if (!stdout) {
      //console.error("Npm version check error: no output!")
      callback(true, false)
      return
    }

    const npm_version = stdout.replace('\n', '')
    callback(false, npm_version)
  })
}

function green(text) {
  return '\u001b[32m' + text + '\u001b[39m';
}

module.exports = {

  getNpmVersion: getNpmVersion,

  checkNpmVersion: function (npmVersion) {

    if (npmVersion && npmVersion != packageJson.version) {
      console.log(green(
`
  There is a newer version of ${packageJson.name} available: ${npmVersion}
  (You are currently using version: ${packageJson.version})

  Install FAR.js globally using the package manager of your choice.
  For example, to get the latest version:

    npm i -g ${packageJson.name}

  See the changelog here: https://github.com/farjs/farjs/releases
`
      ))
    }
  }
}
