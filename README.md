
[![Build Status](https://travis-ci.org/scommons/farclone.svg?branch=master)](https://travis-ci.org/scommons/farclone)
[![Coverage Status](https://coveralls.io/repos/github/scommons/farclone/badge.svg?branch=master)](https://coveralls.io/github/scommons/farclone?branch=master)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.17.svg)](https://www.scala-js.org)

## FARc
File and Archive Manager [FAR](https://farmanager.com/index.php?l=en) clone app build on [Electron](https://electronjs.org/)

### How to Build

To build and run all the tests use the following command:
```bash
sbt test
```

### How to Run

To run the app use the following commands:
```bash
sbt "project farclone-app" fastOptJS::webpack

node ./app/target/scala-2.12/scalajs-bundler/main/reload.index.js
```

To exit the application press `F10` on the keyboard.

### How to Run with Reload Workflow

```bash
#console 1:
sbt
>project farclone-app
>~fastOptJS

#console 2:
cd ./app/target/scala-2.12/scalajs-bundler/main/
./node_modules/webpack/bin/webpack.js --watch --config ./reload.webpack.config.js

#console 3:
node ./app/target/scala-2.12/scalajs-bundler/main/dist/bundle.js
```

## Documentation

You can find more documentation [here](https://scommons.org/)
