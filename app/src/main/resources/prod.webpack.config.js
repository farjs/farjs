const merge = require("webpack-merge")

const commonConfig = require("./common.webpack.config.js")

module.exports = merge(commonConfig, {

  entry: [
    './prod.loader.js'
  ],
  output: {
    filename: '../farjs-app-opt-bundle.js'
  },
  
  mode: 'production'
})
