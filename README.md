
[![Build Status](https://travis-ci.org/scommons/scommons-farc.svg?branch=master)](https://travis-ci.org/scommons/scommons-farc)
[![Coverage Status](https://coveralls.io/repos/github/scommons/scommons-farc/badge.svg?branch=master)](https://coveralls.io/github/scommons/scommons-farc?branch=master)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.17.svg)](https://www.scala-js.org)

## FARc
File and Archive Commander ([FAR](https://farmanager.com/index.php?l=en) clone) build on [Electron](https://electronjs.org/)

### How to Build

To build and run all the tests use the following command:
```bash
sbt test
```

### How to Run

To run the app use the following commands:
```bash
sbt "project scommons-farc-app" fastOptJS

node ./app/target/scala-2.12/scalajs-bundler/main/scommons-farc-app-fastopt.js
```

To exit the application press `F10` on the keyboard.

## Documentation

You can find more documentation [here](https://scommons.org/)
