const {FarjsApp} = require("./farjs-app-fastopt")

var screen = FarjsApp.start(true)

if (module.hot) {
  // remove webpack Hot Module Replacement (HMR) logging
  const _log = console.log
  console.log = function () {
    if (arguments[0].indexOf('[HMR]') == -1) {
      return _log.apply(console, arguments)
    }
  }
  
  module.hot.accept("./farjs-app-fastopt", function () {
    const oldScreen = screen
    oldScreen.destroy()
    
    const {FarjsApp} = require("./farjs-app-fastopt")
    screen = FarjsApp.start(true)
  })
}
