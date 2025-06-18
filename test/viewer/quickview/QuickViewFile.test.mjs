/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { QuickViewFileProps } from "../../../viewer/quickview/QuickViewFile.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import ViewerController from "../../../viewer/ViewerController.mjs";
import QuickViewFile from "../../../viewer/quickview/QuickViewFile.mjs";
import { createViewerFileViewport } from "../../../viewer/ViewerFileViewport.mjs";
import MockViewerFileReader from "../../../viewer/MockViewerFileReader.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

QuickViewFile.viewerController = mockComponent(ViewerController);

const { viewerController } = QuickViewFile;

describe("QuickViewFile.test.mjs", () => {
  it("should update viewport when setViewport", () => {
    //given
    const props = getQuickViewFileProps();
    const renderer = TestRenderer.create(h(QuickViewFile, props));
    const viewProps = renderer.root.findByType(viewerController).props;
    assert.deepEqual(viewProps.viewport, undefined);
    const viewport = createViewerFileViewport(
      new MockViewerFileReader(),
      "uft8",
      123,
      3,
      2
    );

    //when
    viewProps.setViewport(viewport);

    //then
    const updatedViewProps = renderer.root.findByType(viewerController).props;
    assert.deepEqual(updatedViewProps.viewport === viewport, true);
  });

  it("should emit onViewerOpenLeft event when onKeypress(F3)", () => {
    //given
    const dispatch = mockFunction();
    let emitArgs = /** @type {any[]} */ ([]);
    const emit = mockFunction((...args) => (emitArgs = args));
    /** @type {React.MutableRefObject<BlessedElement | null>} */
    const inputRef = React.createRef();
    inputRef.current = /** @type {any} */ ({ emit });
    const props = getQuickViewFileProps({ dispatch, inputRef, isRight: true });
    const renderer = TestRenderer.create(h(QuickViewFile, props));
    const viewProps = renderer.root.findByType(viewerController).props;

    //when
    const result = viewProps.onKeypress("f3");

    //then
    assert.deepEqual(result, true);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(emit.times, 1);
    assert.deepEqual(emitArgs, [
      "keypress",
      undefined,
      { name: "", full: "onViewerOpenLeft" },
    ]);
  });

  it("should emit onViewerOpenRight event when onKeypress(F3)", () => {
    //given
    const dispatch = mockFunction();
    let emitArgs = /** @type {any[]} */ ([]);
    const emit = mockFunction((...args) => (emitArgs = args));
    /** @type {React.MutableRefObject<BlessedElement | null>} */
    const inputRef = React.createRef();
    inputRef.current = /** @type {any} */ ({ emit });
    const props = getQuickViewFileProps({ dispatch, inputRef });
    const renderer = TestRenderer.create(h(QuickViewFile, props));
    const viewProps = renderer.root.findByType(viewerController).props;

    //when
    const result = viewProps.onKeypress("f3");

    //then
    assert.deepEqual(result, true);
    assert.deepEqual(dispatch.times, 0);
    assert.deepEqual(emit.times, 1);
    assert.deepEqual(emitArgs, [
      "keypress",
      undefined,
      { name: "", full: "onViewerOpenRight" },
    ]);
  });

  it("should return false if unknown key when onKeypress", () => {
    //given
    const props = getQuickViewFileProps();
    const renderer = TestRenderer.create(h(QuickViewFile, props));
    const viewProps = renderer.root.findByType(viewerController).props;

    //when
    const result = viewProps.onKeypress("unknown");

    //then
    assert.deepEqual(result, false);
  });

  it("should render component", () => {
    //given
    const props = getQuickViewFileProps();

    //when
    const result = TestRenderer.create(h(QuickViewFile, props)).root;

    //then
    assertQuickViewFile(result, props);
  });
});

/**
 * @param {Partial<QuickViewFileProps>} params
 * @returns {QuickViewFileProps}
 */
function getQuickViewFileProps({
  dispatch = mockFunction(),
  /** @type {React.MutableRefObject<BlessedElement | null>} */ inputRef = React.createRef(),
  isRight = false,
  filePath = "some/file/path",
  size = 123,
} = {}) {
  return {
    dispatch,
    inputRef,
    isRight,
    filePath,
    size,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {QuickViewFileProps} props
 */
function assertQuickViewFile(result, props) {
  assert.deepEqual(QuickViewFile.displayName, "QuickViewFile");

  assertComponents(
    result.children,
    h(viewerController, {
      inputRef: props.inputRef,
      dispatch: props.dispatch,
      filePath: props.filePath,
      size: props.size,
      setViewport: mockFunction(),
      onKeypress: mockFunction(),
    })
  );
}
