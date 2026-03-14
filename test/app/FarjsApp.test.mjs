/**
 * @typedef {import("@farjs/blessed").Widgets.Screen} BlessedScreen
 */
import TestRenderer from "react-test-renderer";
import { deepEqual } from "node:assert/strict";
import mockFunction from "mock-fn";
import { mockComponent } from "react-assert";
import AppRoot from "@farjs/ui/app/AppRoot.mjs";
import DevTool from "@farjs/ui/tool/DevTool.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import testDb from "../db.mjs";
import FSFileListActions from "../../fs/FSFileListActions.mjs";
import FarjsData from "../../app/FarjsData.mjs";
import FarjsApp from "../../app/FarjsApp.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FarjsApp.appRootComp = mockComponent(AppRoot);

/** @type {BlessedScreen} */
const screen = /** @type {any} */ ({});

describe("FarjsApp.test.mjs", () => {
  it("should return screen and call onExit when createScreen/exit", () => {
    //given
    let blesseScreenArgs = /** @type {any[]} */ ([]);
    const blesseScreen = mockFunction((...args) => {
      blesseScreenArgs = args;
      return screen;
    });
    let screenKeyArgs = /** @type {any[]} */ ([]);
    const screenKey = mockFunction((...args) => (screenKeyArgs = args));
    const destroy = mockFunction();
    /** @type {BlessedScreen} */
    const screen = /** @type {any} */ ({
      key: screenKey,
      destroy,
    });
    FarjsApp._blesseScreen = blesseScreen;
    const onExit = mockFunction();

    //when
    const result = FarjsApp.createScreen(onExit);

    //then
    deepEqual(result === screen, true);
    deepEqual(blesseScreen.times, 1);
    deepEqual(blesseScreenArgs, [
      {
        autoPadding: true,
        smartCSR: true,
        tabSize: 1,
        fullUnicode: true,
        cursorShape: "underline",
      },
    ]);
    deepEqual(screenKey.times, 1);
    deepEqual(screenKeyArgs.slice(0, 1), [["C-e"]]);
    const keyListener = screenKeyArgs[1];

    //when
    keyListener();

    //then
    deepEqual(destroy.times, 1);
    deepEqual(onExit.times, 1);
  });

  it("should return screen and call process.exit when createScreen/exit", () => {
    //given
    let blesseScreenArgs = /** @type {any[]} */ ([]);
    const blesseScreen = mockFunction((...args) => {
      blesseScreenArgs = args;
      return screen;
    });
    let screenKeyArgs = /** @type {any[]} */ ([]);
    const screenKey = mockFunction((...args) => (screenKeyArgs = args));
    const destroy = mockFunction();
    /** @type {BlessedScreen} */
    const screen = /** @type {any} */ ({
      key: screenKey,
      destroy,
    });
    FarjsApp._blesseScreen = blesseScreen;
    const savedConsoleLog = console.log;
    const savedConsoleError = console.error;
    const savedProcessExit = process.exit;
    const processExit = mockFunction();
    //@ts-ignore
    process.exit = processExit;

    //when
    const result = FarjsApp.createScreen();

    //then
    deepEqual(result === screen, true);
    deepEqual(blesseScreen.times, 1);
    deepEqual(blesseScreenArgs, [
      {
        autoPadding: true,
        smartCSR: true,
        tabSize: 1,
        fullUnicode: true,
        cursorShape: "underline",
      },
    ]);
    deepEqual(screenKey.times, 1);
    deepEqual(screenKeyArgs.slice(0, 1), [["C-e"]]);
    const keyListener = screenKeyArgs[1];
    console.log = mockFunction();
    console.error = mockFunction();

    //when
    keyListener();

    //then
    deepEqual(console.log === savedConsoleLog, true);
    deepEqual(console.error === savedConsoleError, true);
    console.log = savedConsoleLog;
    console.error = savedConsoleError;
    process.exit = savedProcessExit;
    deepEqual(destroy.times, 1);
    deepEqual(processExit.times, 1);
  });

  it("should render with DevTool.Logs when render(showDevTools = true)", () => {
    //given
    let rendererArgs = /** @type {any[]} */ ([]);
    const renderer = mockFunction((...args) => {
      rendererArgs = args;
      return screen;
    });
    FarjsApp._renderer = renderer;

    //when
    FarjsApp.render(true, screen);

    //then
    deepEqual(renderer.times, 1);
    deepEqual(rendererArgs.slice(1, 2), [screen]);
    const appElement = rendererArgs[0];

    //when
    const result = TestRenderer.create(appElement).root;

    //then
    deepEqual(result.type, FarjsApp.appRootComp);
    deepEqual(result.props, {
      loadMainUi: result.props.loadMainUi,
      initialDevTool: DevTool.Logs,
      defaultTheme: FileListTheme.defaultTheme,
    });
  });

  it("should render with DevTool.Hidden when render(showDevTools = false)", () => {
    //given
    let rendererArgs = /** @type {any[]} */ ([]);
    const renderer = mockFunction((...args) => {
      rendererArgs = args;
      return screen;
    });
    FarjsApp._renderer = renderer;

    //when
    FarjsApp.render(false, screen);

    //then
    deepEqual(renderer.times, 1);
    deepEqual(rendererArgs.slice(1, 2), [screen]);
    const appElement = rendererArgs[0];

    //when
    const result = TestRenderer.create(appElement).root;

    //then
    deepEqual(result.type, FarjsApp.appRootComp);
    deepEqual(result.props, {
      loadMainUi: result.props.loadMainUi,
      initialDevTool: DevTool.Hidden,
      defaultTheme: FileListTheme.defaultTheme,
    });
  });

  it("should return xterm-256color theme when loadMainUi", async () => {
    //given
    /** @type {BlessedScreen} */
    const screen = /** @type {any} */ ({ terminal: "xterm-256color" });
    let rendererArgs = /** @type {any[]} */ ([]);
    const renderer = mockFunction((...args) => {
      rendererArgs = args;
      return screen;
    });
    FarjsApp._renderer = renderer;
    FarjsApp.render(false, screen);
    deepEqual(renderer.times, 1);
    deepEqual(rendererArgs.slice(1, 2), [screen]);
    const appElement = rendererArgs[0];
    const comp = TestRenderer.create(appElement).root;
    const loadMainUi = comp.props.loadMainUi;
    let prepareDBArgs = /** @type {any[]} */ ([]);
    const prepareDB = mockFunction((...args) => {
      prepareDBArgs = args;
      return testDb();
    });
    let createPortalsArgs = /** @type {any[]} */ ([]);
    const createPortals = mockFunction((...args) => {
      createPortalsArgs = args;
      return () => null;
    });
    FarjsApp._prepareDB = prepareDB;
    FarjsApp._createPortals = createPortals;

    //when
    const result = await loadMainUi();

    //then
    deepEqual(result, {
      theme: FileListTheme.xterm256Theme,
      mainUi: result.mainUi,
    });
    deepEqual(prepareDB.times, 1);
    deepEqual(prepareDBArgs, [FSFileListActions.instance, FarjsData.instance]);
    deepEqual(createPortals.times, 1);
    deepEqual(createPortalsArgs, [screen]);
  });

  it("should return default theme and call onReady when loadMainUi", async () => {
    //given
    const onReady = mockFunction();
    let rendererArgs = /** @type {any[]} */ ([]);
    const renderer = mockFunction((...args) => {
      rendererArgs = args;
      return screen;
    });
    FarjsApp._renderer = renderer;
    FarjsApp.render(false, screen, onReady);
    deepEqual(renderer.times, 1);
    deepEqual(rendererArgs.slice(1, 2), [screen]);
    const appElement = rendererArgs[0];
    const comp = TestRenderer.create(appElement).root;
    const loadMainUi = comp.props.loadMainUi;
    let prepareDBArgs = /** @type {any[]} */ ([]);
    const prepareDB = mockFunction((...args) => {
      prepareDBArgs = args;
      return testDb();
    });
    let createPortalsArgs = /** @type {any[]} */ ([]);
    const createPortals = mockFunction((...args) => {
      createPortalsArgs = args;
      return () => null;
    });
    FarjsApp._prepareDB = prepareDB;
    FarjsApp._createPortals = createPortals;

    //when
    const result = await loadMainUi();

    //then
    deepEqual(result, {
      theme: FileListTheme.defaultTheme,
      mainUi: result.mainUi,
    });
    deepEqual(onReady.times, 1);
    deepEqual(prepareDB.times, 1);
    deepEqual(prepareDBArgs, [FSFileListActions.instance, FarjsData.instance]);
    deepEqual(createPortals.times, 1);
    deepEqual(createPortalsArgs, [screen]);
  });

  it("should create new screen and call render when start", () => {
    //given
    const onReady = mockFunction();
    const onExit = mockFunction();
    let createScreenArgs = /** @type {any[]} */ ([]);
    const createScreen = mockFunction((...args) => {
      createScreenArgs = args;
      return screen;
    });
    let renderArgs = /** @type {any[]} */ ([]);
    const render = mockFunction((...args) => (renderArgs = args));
    FarjsApp.createScreen = createScreen;
    FarjsApp.render = render;

    //when
    FarjsApp.start(true, onReady, onExit);

    //then
    deepEqual(createScreen.times, 1);
    deepEqual(createScreenArgs, [onExit]);
    deepEqual(render.times, 1);
    deepEqual(renderArgs, [true, screen, onReady]);
  });
});
