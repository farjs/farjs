## Developing

### Install node dependencies

```bash
npm i
```

### How to Build

To lint and run all the tests use the following command:

```bash
npx quick-lint-js ./**/**.mjs && npm run test
```

### How to Run

To run the app use the following command:

```bash
node ./dist/farjs.mjs
```

### How to Run with Reload Workflow

```bash
node --trace-deprecation --watch ./dist/farjs.dev.mjs
```
