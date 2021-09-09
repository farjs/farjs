import babel from 'rollup-plugin-babel'
import commonjs from 'rollup-plugin-commonjs'
import resolve from 'rollup-plugin-node-resolve'

import pkg from './package.json'

export default {
  input: './farjs-app-opt-bundle.js',
  output: [
    {
      file: './dist/far.js',
      format: 'cjs',
      sourcemap: false
    }
  ],
  external: Object.keys(pkg.dependencies).concat([
    'fs', 'path', 'child_process', 'events', 'string_decoder'
  ]),
  plugins: [
    babel({
      exclude: 'node_modules/**'
    }),
    resolve(),
    commonjs({
      ignore: Object.keys(pkg.dependencies)
    })
  ]
}
