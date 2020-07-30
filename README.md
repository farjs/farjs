
[![Build Status](https://travis-ci.org/scommons/far-js.svg?branch=master)](https://travis-ci.org/scommons/far-js)
[![Coverage Status](https://coveralls.io/repos/github/scommons/far-js/badge.svg?branch=master)](https://coveralls.io/github/scommons/far-js?branch=master)
[![npm version](https://img.shields.io/npm/v/farjs-app)](https://www.npmjs.com/package/farjs-app)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.17.svg)](https://www.scala-js.org)

## FAR.js

Good old Windows File and Archive Manager
([FAR](https://farmanager.com/index.php?l=en)) app built with:
  [Scala.js](https://www.scala-js.org/)
  [React.js](https://reactjs.org/)
  [react-blessed](https://github.com/Yomguithereal/react-blessed)
  [blessed](https://github.com/chjj/blessed)

### Install

To install (or upgrade) it on your machine use the following command:

``` bash
$ npm i -g farjs-app
```

then you can run the application from your favorite terminal:

``` bash
$ farjs
```

![Screenshots](https://raw.githubusercontent.com/scommons/far-js/master/docs/images/screenshots.png)

### How to Build

To build and run all the tests use the following command:
```bash
sbt test
```

### How to Run

To run the app use the following commands:
```bash
sbt "project farjs-app" fastOptJS::webpack

node ./app/target/scala-2.12/scalajs-bundler/main/reload.index.js
```

To exit the application press `F10` on the keyboard.

### How to Run with Reload Workflow

```bash
#console 1:
sbt
>project farjs-app
>~fastOptJS

#console 2:
cd ./app/target/scala-2.12/scalajs-bundler/main/
./node_modules/webpack/bin/webpack.js --watch --config ./reload.webpack.config.js

#console 3:
node ./app/target/scala-2.12/scalajs-bundler/main/dist/bundle.js
```

## Documentation

You can find more documentation [here](https://scommons.org/)
