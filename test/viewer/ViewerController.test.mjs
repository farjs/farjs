/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("../../viewer/ViewerController.mjs").ViewerControllerProps} ViewerControllerProps
 * @import { TaskAction } from "@farjs/ui/task/TaskAction.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponent, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import WithSize from "@farjs/ui/WithSize.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import Encoding from "../../file/Encoding.mjs";
import FileViewHistory from "../../file/FileViewHistory.mjs";
import MockViewerFileReader from "../../viewer/MockViewerFileReader.mjs";
import { createViewerFileViewport } from "../../viewer/ViewerFileViewport.mjs";
import ViewerFileLine from "../../viewer/ViewerFileLine.mjs";
import ViewerContent from "../../viewer/ViewerContent.mjs";
import ViewerController from "../../viewer/ViewerController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewerController.withSizeComp = mockComponent(WithSize);
ViewerController.viewerContent = mockComponent(ViewerContent);

const { withSizeComp, viewerContent } = ViewerController;

describe("ViewerController.test.mjs", () => {
  it("should dispatch error task if failed to open file when mount", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return openP;
    });
    const error = Error("test error");
    const openP = Promise.reject(error);
    const fileReader = new MockViewerFileReader({ open });
    ViewerController._createFileReader = () => fileReader;
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let getOneArgs = /** @type {any[]} */ ([]);
    const getOne = mockFunction((...args) => {
      getOneArgs = args;
      return getOneP;
    });
    const service = new MockHistoryService({ getOne });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const getOneP = Promise.resolve(undefined);
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const props = getViewerControllerProps({ dispatch });

    //when
    TestRenderer.create(
      withThemeContext(
        withHistoryProvider(h(ViewerController, props), historyProvider)
      )
    );

    //then
    await getP;
    await getOneP;
    await Promise.resolve();
    await Promise.resolve();
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getOne.times, 1);
    assert.deepEqual(getOneArgs, [
      FileViewHistory.pathToItem(props.filePath, false),
    ]);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [props.filePath]);
    assert.deepEqual(dispatch.times, 1);
    /** @type {TaskAction<?>} */
    const action = dispatchArgs[0];
    let resultError = null;
    try {
      await action.task.result;
    } catch (e) {
      resultError = e;
    }
    assert.deepEqual(resultError, error);
  });

  it("should open/close file and use default viewport params if no history when mount/unmount", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return openP;
    });
    const close = mockFunction(() => closeP);
    const openP = Promise.resolve();
    const closeP = Promise.resolve();
    const fileReader = new MockViewerFileReader({ open, close });
    ViewerController._createFileReader = () => fileReader;
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let getOneArgs = /** @type {any[]} */ ([]);
    const getOne = mockFunction((...args) => {
      getOneArgs = args;
      return getOneP;
    });
    const service = new MockHistoryService({ getOne });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const getOneP = Promise.resolve(undefined);
    let setViewportArgs = /** @type {any[]} */ ([]);
    const setViewport = mockFunction((...args) => (setViewportArgs = args));
    const props = getViewerControllerProps({ setViewport });

    //when
    const renderer = TestRenderer.create(
      withThemeContext(
        withHistoryProvider(h(ViewerController, props), historyProvider)
      )
    );

    //then
    await getP;
    await getOneP;
    await openP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getOne.times, 1);
    assert.deepEqual(getOneArgs, [
      FileViewHistory.pathToItem(props.filePath, false),
    ]);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [props.filePath]);
    assert.deepEqual(setViewport.times, 1);
    const resViewport = setViewportArgs[0];
    assert.deepEqual(
      resViewport,
      createViewerFileViewport(
        fileReader,
        Encoding.platformEncoding,
        props.size,
        0,
        0,
        false,
        0,
        0,
        []
      )
    );
    assertViewerController(renderer.root, props);

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //then
    assert.deepEqual(close.times, 1);
  });

  it("should open/close file and use viewport params from history when mount/unmount", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return openP;
    });
    const close = mockFunction(() => closeP);
    const openP = Promise.resolve();
    const closeP = Promise.resolve();
    const fileReader = new MockViewerFileReader({ open, close });
    ViewerController._createFileReader = () => fileReader;
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let getOneArgs = /** @type {any[]} */ ([]);
    const getOne = mockFunction((...args) => {
      getOneArgs = args;
      return getOneP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ getOne, save });
    const historyProvider = new MockHistoryProvider({ get });
    let setViewportArgs = /** @type {any[]} */ ([]);
    const setViewport = mockFunction((...args) => (setViewportArgs = args));
    const props = getViewerControllerProps({ setViewport });
    const history = FileViewHistory(props.filePath, {
      isEdit: false,
      encoding: "test-enc",
      position: 456,
      wrap: true,
      column: 7,
    });
    const getP = Promise.resolve(service);
    const getOneP = Promise.resolve(FileViewHistory.toHistory(history));
    const saveP = Promise.resolve();

    //when
    const renderer = TestRenderer.create(
      withThemeContext(
        withHistoryProvider(h(ViewerController, props), historyProvider)
      )
    );

    //then
    await getP;
    await getOneP;
    await openP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getOne.times, 1);
    assert.deepEqual(getOneArgs, [
      FileViewHistory.pathToItem(props.filePath, false),
    ]);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [props.filePath]);
    assert.deepEqual(setViewport.times, 1);
    const resViewport = setViewportArgs[0];
    assert.deepEqual(
      resViewport,
      createViewerFileViewport(
        fileReader,
        history.params.encoding,
        props.size,
        0,
        0,
        history.params.wrap,
        history.params.column,
        history.params.position,
        []
      )
    );
    assertViewerController(renderer.root, props);
    const updatedProps = { ...props, viewport: resViewport };

    //when & then
    TestRenderer.act(() => {
      renderer.update(
        withThemeContext(
          withHistoryProvider(
            h(ViewerController, updatedProps),
            historyProvider
          )
        )
      );
    });
    assertViewerController(renderer.root, updatedProps);

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //then
    await saveP;
    assert.deepEqual(close.times, 1);
    assert.deepEqual(get.times, 2);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(save.times, 1);
    const saveHistory = saveArgs[0];
    assert.deepEqual(FileViewHistory.fromHistory(saveHistory), history);
  });

  it("should render left and right scroll indicators", async () => {
    //given
    let openArgs = /** @type {any[]} */ ([]);
    const open = mockFunction((...args) => {
      openArgs = args;
      return openP;
    });
    const openP = Promise.resolve();
    const fileReader = new MockViewerFileReader({ open });
    ViewerController._createFileReader = () => fileReader;
    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let getOneArgs = /** @type {any[]} */ ([]);
    const getOne = mockFunction((...args) => {
      getOneArgs = args;
      return getOneP;
    });
    const service = new MockHistoryService({ getOne });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const getOneP = Promise.resolve(undefined);
    const props = getViewerControllerProps();
    const renderer = TestRenderer.create(
      withThemeContext(
        withHistoryProvider(h(ViewerController, props), historyProvider)
      )
    );
    await getP;
    await getOneP;
    await openP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [FileViewHistory.fileViewsHistoryKind]);
    assert.deepEqual(getOne.times, 1);
    assert.deepEqual(getOneArgs, [
      FileViewHistory.pathToItem(props.filePath, false),
    ]);
    assert.deepEqual(open.times, 1);
    assert.deepEqual(openArgs, [props.filePath]);
    assertViewerController(renderer.root, props);
    const updatedProps = {
      ...props,
      viewport: createViewerFileViewport(
        fileReader,
        "win",
        123,
        3,
        2,
        false,
        1,
        0,
        [ViewerFileLine("test", 4), ViewerFileLine("test content", 12)]
      ),
    };

    //when
    TestRenderer.act(() => {
      renderer.update(
        withThemeContext(
          withHistoryProvider(
            h(ViewerController, updatedProps),
            historyProvider
          )
        )
      );
    });

    //then
    assertViewerController(renderer.root, updatedProps, [1]);
  });
});

/**
 * @param {Partial<ViewerControllerProps>} params
 * @returns {ViewerControllerProps}
 */
function getViewerControllerProps({
  dispatch = mockFunction(),
  filePath = "test/file",
  size = 10,
  viewport,
  setViewport = () => {},
  onKeypress = () => false,
} = {}) {
  const inputRef =
    /** @type {React.MutableRefObject<BlessedElement | null>} */ (
      React.createRef()
    );
  return {
    inputRef,
    dispatch,
    filePath,
    size,
    viewport,
    setViewport,
    onKeypress,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ViewerControllerProps} props
 * @param {readonly number[]} [scrollIndicators]
 */
function assertViewerController(result, props, scrollIndicators = []) {
  assert.deepEqual(ViewerController.displayName, "ViewerController");

  const theme = FileListTheme.defaultTheme;

  assertComponents(
    result.children,
    h(withSizeComp, { render: mockFunction() })
  );
  const width = 3;
  const height = 2;
  const render = result.findByType(withSizeComp).props.render;
  const renderRes = render(width, height);
  const resComp = TestRenderer.create(renderRes).root;
  const viewport = props.viewport;

  assertComponent(
    resComp,
    h(
      "box",
      {
        style: ViewerContent.contentStyle(theme),
      },
      ...(() => {
        if (viewport) {
          const linesCount = viewport.linesData.length;
          const resViewport = resComp.findByType(viewerContent).props.viewport;

          return [
            h(viewerContent, {
              inputRef: props.inputRef,
              viewport: { ...resViewport, width, height },
              setViewport: mockFunction(),
              onKeypress: mockFunction(),
            }),

            viewport.column > 0 && linesCount > 0
              ? h("text", {
                  style: ViewerController.scrollStyle(theme),
                  width: 1,
                  height: linesCount,
                  content: "<".repeat(linesCount),
                })
              : null,

            ...scrollIndicators.map((lineIdx) => {
              return h("text", {
                style: ViewerController.scrollStyle(theme),
                left: width - 1,
                top: lineIdx,
                width: 1,
                height: 1,
                content: ">",
              });
            }),
          ].filter((_) => _ !== null);
        }

        return [];
      })()
    )
  );
}
