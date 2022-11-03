const { merge } = require("webpack-merge")

const commonConfig = require("./common.webpack.config.js")

module.exports = merge(commonConfig, {

  output: {
    libraryTarget: 'commonjs'
  },

  mode: 'development'
})
