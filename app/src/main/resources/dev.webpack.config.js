const { merge } = require("webpack-merge")

const commonConfig = require("./common.webpack.config.js")

module.exports = merge(commonConfig, {

  entry: [
    './dev.loader.js'
  ],
  output: {
    filename: '../farjs-app-fastopt-bundle.js'
  },
  
  mode: 'development'
})
