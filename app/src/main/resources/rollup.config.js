const commonjs = require('@rollup/plugin-commonjs')
const resolve = require('@rollup/plugin-node-resolve')
const replace = require('@rollup/plugin-replace')
const compiler = require('@ampproject/rollup-plugin-closure-compiler')

const pkg = require('./package.json')

module.exports = {
  input: [
//    'internal-efbde62afaf544cddf4a04c6408e586c767909f3.js',
    'ui.js',
    'farjs-app-opt.js'
  ],
  output: [
    {
      dir: './build',
      format: 'cjs',
      sourcemap: false
    }
  ],
  external: Object.keys(pkg.dependencies).concat([
    'os', 'fs', 'path', 'child_process', 'events', 'string_decoder', './farjs/domain/bundle.json'
  ]),
  plugins: [
    resolve(),
    replace({
      preventAssignment: true,
      delimiters: ['', ''],
      values: {
        '\\uff3f': "__" // see: https://github.com/google/closure-compiler/issues/2851
      }
    }),
    commonjs({
      ignore: Object.keys(pkg.dependencies)
    }),
    compiler({
      // see: https://github.com/google/closure-compiler/wiki/Flags-and-Options
//      compilation_level: 'ADVANCED',
      env: 'CUSTOM',
      externs: './gcc-externs.js',
      language_in: 'ECMASCRIPT5_STRICT',
      module_resolution: 'NODE',
    })
  ]
}
