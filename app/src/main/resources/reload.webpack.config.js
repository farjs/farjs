const nodeExternals = require('webpack-node-externals');
const webpack = require('webpack');
const path = require('path');

// Setup webpack Hot Module Replacement (HMR)
// see: https://github.com/webpack/docs/issues/45
//
module.exports = {

  mode: 'development',
  
  entry: [
    'webpack/hot/poll?1000',
    './reload.loader.js'
  ],
  output: {
    path: path.resolve(__dirname, '.'),
    filename: 'farjs-app-fastopt-hotreload.js'
  },
  
  target: 'node', // important in order not to bundle built-in modules like path, fs, etc.  
  
  externals: [
    nodeExternals({ // in order to ignore modules in node_modules folder from bundling
      allowlist: ['webpack/hot/poll?1000', 'react-redux']
    }),
    { "react-redux": "commonjs react-redux/lib/alternate-renderers" }
  ],
  
  plugins: [
      new webpack.HotModuleReplacementPlugin()
  ]
};
