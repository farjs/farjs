## Developing

### Install node dependencies

```bash
npm i --install-links=true
```

### How to Build

To build and run all the tests use the following command:

```bash
sbt test
```

### How to Run

To run the app use the following commands:

```bash
sbt "project farjs-app" fastOptJS

node ./dist/farjs.dev.mjs
```

### How to Run with Reload Workflow

```bash
#console 1:
sbt
>project farjs-app
>~fastOptJS

#console 2:
node --trace-deprecation --watch ./dist/farjs.dev.mjs
```

## Resources

You can find more info about the common modules [here](https://scommons.github.io/)
