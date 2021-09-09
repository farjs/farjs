const nodeExternals = require('webpack-node-externals');

module.exports = {

  target: 'node', // important in order not to bundle built-in modules like path, fs, etc.  

  externals: [
    nodeExternals({ // in order to ignore all modules in node_modules folder from bundling
      whitelist: ['react-redux']
    }),
    { "react-redux": "commonjs react-redux/lib/alternate-renderers" }
  ]
};
