
[![Build Status](https://travis-ci.com/scommons/far-js.svg?branch=master)](https://travis-ci.com/scommons/far-js)
[![Coverage Status](https://coveralls.io/repos/github/scommons/far-js/badge.svg?branch=master)](https://coveralls.io/github/scommons/far-js?branch=master)
[![npm version](https://img.shields.io/npm/v/farjs-app)](https://www.npmjs.com/package/farjs-app)
[![Rate on Openbase](https://badges.openbase.com/js/rating/farjs-app.svg)](https://openbase.com/js/farjs-app?utm_source=embedded&utm_medium=badge&utm_campaign=rate-badge)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.29.svg)](https://www.scala-js.org)

## FAR.js

Good old Windows **F**ile and **AR**chive Manager
([FAR](https://en.wikipedia.org/wiki/Far_Manager)) app built with:
  [Scala.js](https://www.scala-js.org/),
  [React.js](https://reactjs.org/),
  [react-blessed](https://github.com/Yomguithereal/react-blessed),
  [blessed](https://github.com/chjj/blessed)

Runs on [Node.js](https://nodejs.org/), thus cross-platform:
* `Mac OS` (primary support in [iTerm2](https://iterm2.com/) terminal)
* `Linux`
* `Windows`

## Install

To install (or upgrade) it on your machine use the following command:

``` bash
$ npm i -g farjs-app
```

then you can run the application from your favorite terminal:

``` bash
$ farjs
```

![Screenshots](https://raw.githubusercontent.com/scommons/far-js/master/docs/images/screenshots.png)

To exit the application - press `F10` on the keyboard.

## Documentation

### Modules

- [File Browser](#file-browser)
- [Dev Tools](#dev-tools)
  - [Logs](#logs)
  - [Colors](#colors)

### Other

- Developing
  - See [develop.md](https://github.com/scommons/far-js/blob/master/develop.md)
- [FAQ](#faq)
  - [Key Bindings](#key-bindings)

## File Browser

Main application window that consists of two similar panels.
Each panel displays list of files and directories. You can perform
different operations:

* **Navigation** within panels:
  * Items **selection** - `Shift + Up/Down/Left/Right/PageUp/PageDown/Home/End`
  * **Go back** to the parent folder - `Ctrl + PageUp`
  * **Go into** a folder - `Ctrl + PageDown` / `Return`

* **Open item** in default application - `Alt + PageDown`
  (see [Key Bindings](#key-bindings) for how to re-map it to `Shift + Return`)
* **Copy Path** of current item into **Clipboard** - `Ctrl + C`
  (in iTerm2 only)
* **Swap** the panels - `Ctrl + U`
* **Quick View** of current item on in-active panel - `Ctrl + Q`
* Show **Quick Search** box - `Ctrl + S`
* **Refresh** active panel - `Ctrl + R`
* **View item**(s) - `F3`
  * Scans selected folder(s)/file(s) and calculates size(s)
* **Copy item**(s) - `F5`
* **Create folder** (with intermediate sub-folders) - `F7`
* **Delete item**(s) - `F8`

## Dev Tools

Use `F12` to show/hide DEV tools components on the right side.
Press `F12` again to switch between the components.

### Logs

Shows all the intercepted `console.log` and `console.error` messages,
since the app itself is rendered to the console.

### Colors

Shows possible colors with their `hex` codes for current terminal.

## FAQ

### Key Bindings

* Why supported key combination doesn't work or trigger another
action in my terminal?
  - You may re-map the keys to **send** supported **escape sequences**
  or **hex codes**.
  For example you can re-map:
    - | Key | Supported Key | Escape Sequence ^[ ... | Hex Codes |
      | --- | --- | --- | --- |
      | `Shift + Return` | `Alt + PageDown` | `[6;3~` |
      | `CMD + PageDown` | `Ctrl + PageDown` | `[6^` |
      | `CMD + PageUp` | `Ctrl + PageUp` | `[5^` |
      | `CMD + R` | `Ctrl + R` | | `0x12` |
  - In [iTerm2](https://iterm2.com/) it looks like this:
    - ![Keys Re Mapping](https://raw.githubusercontent.com/scommons/far-js/master/docs/images/keys_re_mapping.png)
  
