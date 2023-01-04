const moduleAlias = require('module-alias')

// see:
//  https://www.npmjs.com/package/module-alias
//
moduleAlias.addAliases({
  'react-redux': 'react-redux/lib/alternate-renderers'
})
