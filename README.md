
[![CI](https://github.com/scommons/far-js/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/scommons/far-js/actions/workflows/ci.yml?query=workflow%3Aci+branch%3Amaster)
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
* `Mac OS X` (primary support in [iTerm2](https://iterm2.com/) terminal)
* `Windows` (primary support in [Windows Terminal](https://docs.microsoft.com/en-us/windows/terminal/))
* `Linux`

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
  - [Inputs](#inputs)
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

* **Drive** selection popup
  (see [Key Bindings](#key-bindings) for how to re-map it to `Alt + F1/F2`)
  * Show it on the **left** panel - `Alt + L`
  * Show it on the **right** panel - `Alt + R`

* **Open item** in default application - `Alt + O`
  (see [Key Bindings](#key-bindings) for how to re-map it to `Shift + Return`)
* **Copy Path** of current item into **Clipboard** - `Ctrl + C`
  (in iTerm2 only)
* **Swap** the panels - `Ctrl + U`
* **Quick View** of current item on in-active panel - `Ctrl + Q`
* Show **Quick Search** box - `Ctrl + S`
* **Refresh** active panel - `Ctrl + R`
* **View item(s)** - `F3`
  * Scans selected folder(s)/file(s) and calculates size(s)
* **Copy item(s)** - `F5`
* **Copy current item inplace** - `Shift + F5`
* **Rename/Move item(s)** - `F6`
* **Rename/Move current item inplace** - `Shift + F6`
* **Create folder** (with intermediate sub-folders) - `F7`
* **Delete item(s)** - `F8`

## Dev Tools

Use `F12` to show/hide DEV tools components on the right side.
Press `F12` again to switch between the components.

### Logs

Shows all the intercepted `console.log` and `console.error` messages,
since the app itself is rendered to the console.

### Inputs

Shows input keys sequences.

### Colors

Shows possible colors with their `hex` codes for current terminal/theme.

## FAQ

### Key Bindings

* Why supported key combination doesn't work or trigger another
action in my terminal?
  - Some key combinations (especially `Alt+`) have to be manually re-mapped
  in your terminal settings to **send** supported **escape sequences**
  or **hex codes**.
  For example, you can re-map:
    - | Key | Supported Key | Escape Sequence ^[ ... | Hex Codes |
      | --- | --- | --- | --- |
      | `Alt + F1` | `Alt + L` | `l` |
      | `Alt + F2` | `Alt + R` | `r` |
      | `Shift + Return` | `Alt + O` | `o` |
      | `CMD + PageDown` | `Ctrl + PageDown` | `[6^` |
      | `CMD + PageUp` | `Ctrl + PageUp` | `[5^` |
      | `CMD + R` | `Ctrl + R` | | `0x12` |
  - In [iTerm2](https://iterm2.com/) it looks like this:
    - ![Keys Re Mapping](https://raw.githubusercontent.com/scommons/far-js/master/docs/images/keys_re_mapping.png)
  - In [Windows Terminal](https://docs.microsoft.com/en-us/windows/terminal/)
  you can use [sendInput action](https://docs.microsoft.com/en-us/windows/terminal/customize-settings/actions#send-input):
    ```json
    //in settings.json
    "actions": [

        { "command": { "action": "sendInput", "input": "\u001bl" }, "keys": "alt+f1" },
        { "command": { "action": "sendInput", "input": "\u001br" }, "keys": "alt+f2" },
        { "command": { "action": "sendInput", "input": "\u001bo" }, "keys": "shift+enter" }

    ]
    ```
