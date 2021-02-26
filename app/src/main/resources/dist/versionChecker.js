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

  check: function (callback) {
    getNpmVersion((error, npmVersion) => {
      if (error) {
        callback()
        return
      }

      if (npmVersion != packageJson.version) {
        console.log(green(
`
  There is a new version of ${packageJson.name} available (${npmVersion}).
  You are currently using ${packageJson.name} ${packageJson.version}
  Install FAR.js globally using the package manager of your choice;
  for example:

    npm install -g ${packageJson.name}

  to get the latest version. See the changelog here:
    https://github.com/scommons/far-js/releases
`
        ))
      }
      callback()
    })
  }
}
