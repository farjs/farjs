import babel from 'rollup-plugin-babel'
import commonjs from 'rollup-plugin-commonjs'
import external from 'rollup-plugin-peer-deps-external'
import resolve from 'rollup-plugin-node-resolve'

import pkg from './package.json'

export default {
  input: './farjs-app-opt.js',
  output: [
    {
      file: './dist/far.js',
      format: 'cjs',
      sourcemap: false
    }
  ],
  external: Object.keys(pkg.dependencies).concat([
    'fs', 'path'
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
