const merge = require("webpack-merge")

const commonConfig = require("./common.webpack.config.js")

module.exports = merge(commonConfig, {

  entry: [
    './farjs-app-opt.js'
  ],
  output: {
    filename: '../farjs-app-opt-bundle.js',
    libraryTarget: 'commonjs'
  },
  
  mode: 'production'
})
