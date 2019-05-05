const {FarcApp} = require("./farclone-app-fastopt")

var screen = FarcApp.start()

if (module.hot) {
  // remove webpack Hot Module Replacement (HMR) logging
  const _log = console.log
  console.log = function () {
    if (arguments[0].indexOf('[HMR]') == -1) {
      return _log.apply(console, arguments)
    }
  }
  
  module.hot.accept("./farclone-app-fastopt", function () {
    const oldScreen = screen
    oldScreen.destroy()
    
    const {FarcApp} = require("./farclone-app-fastopt")
    screen = FarcApp.start()
  })
}
