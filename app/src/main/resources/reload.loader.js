const {FarjsApp} = require("./farjs-app-fastopt")

const showDevTools = true
const screen = FarjsApp.start(showDevTools)

if (module.hot) {
  // remove webpack Hot Module Replacement (HMR) logging
  const _log = console.log
  console.log = function () {
    if (arguments[0].indexOf('[HMR]') == -1) {
      return _log.apply(console, arguments)
    }
  }
  
  module.hot.accept("./farjs-app-fastopt", function () {
    const {FarjsApp} = require("./farjs-app-fastopt")

    FarjsApp.start(showDevTools, screen)
  })
}
