/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {{
 *    full: string,
 * }} IKeyEventArg
 * @typedef {import("../../viewer/ViewerInput.mjs").ViewerInputProps} ViewerInputProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents } from "react-assert";
import mockFunction from "mock-fn";
import ViewerInput from "../../viewer/ViewerInput.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("ViewerInput.test.mjs", () => {
  it("should call onKeypress when keypress event", () => {
    //given
    let onKeypressArgs = /** @type {any[]} */ ([]);
    const onKeypress = mockFunction((...args) => (onKeypressArgs = args));
    /** @type {(ch: any, key: IKeyEventArg) => void} */
    let keyListener = () => {};
    const onMock = mockFunction((event, listener) => {
      if (event === "keypress") {
        keyListener = listener;
      }
    });
    const offMock = mockFunction();
    const props = getViewerInputProps(
      { onKeypress },
      /** @type {any} */ ({ on: onMock, off: offMock })
    );
    TestRenderer.create(h(ViewerInput, props));
    const key = "test_key";

    //when
    keyListener(null, {
      full: key,
    });

    //then
    assert.deepEqual(onMock.times, 3);
    assert.deepEqual(offMock.times, 0);
    assert.deepEqual(onKeypress.times, 1);
    assert.deepEqual(onKeypressArgs, [key]);
  });

  it("should call onWheel when wheelup/wheeldown event", () => {
    //given
    let onWheelArgs1 = /** @type {any[]} */ ([]);
    let onWheelArgs2 = /** @type {any[]} */ ([]);
    const onWheel = mockFunction((...args) => {
      if (onWheel.times === 1) onWheelArgs1 = args;
      else onWheelArgs2 = args;
    });
    let wheelupListener = () => {};
    let wheeldownListener = () => {};
    const onMock = mockFunction((event, listener) => {
      if (event === "wheelup") {
        wheelupListener = listener;
      }
      if (event === "wheeldown") {
        wheeldownListener = listener;
      }
    });
    const offMock = mockFunction();
    const props = getViewerInputProps(
      { onWheel },
      /** @type {any} */ ({ on: onMock, off: offMock })
    );
    TestRenderer.create(h(ViewerInput, props));

    //when & then
    wheelupListener();
    assert.deepEqual(onWheel.times, 1);
    assert.deepEqual(onWheelArgs1, [true]);
    assert.deepEqual(onWheelArgs2, []);

    //when & then
    wheeldownListener();
    assert.deepEqual(onWheel.times, 2);
    assert.deepEqual(onWheelArgs1, [true]);
    assert.deepEqual(onWheelArgs2, [false]);

    //then
    assert.deepEqual(onMock.times, 3);
    assert.deepEqual(offMock.times, 0);
  });

  it("should remove listeners when unmount", () => {
    //given
    let keyListener = () => {};
    let wheelupListener = () => {};
    let wheeldownListener = () => {};
    const onMock = mockFunction((event, listener) => {
      if (event === "keypress") {
        keyListener = listener;
      }
      if (event === "wheelup") {
        wheelupListener = listener;
      }
      if (event === "wheeldown") {
        wheeldownListener = listener;
      }
    });
    const capturedOffListeners = /** @type {any[]} */ ([]);
    const offMock = mockFunction((n, l) => capturedOffListeners.push([n, l]));
    const props = getViewerInputProps(
      {},
      /** @type {any} */ ({ on: onMock, off: offMock })
    );
    const renderer = TestRenderer.create(h(ViewerInput, props));

    //when
    TestRenderer.act(() => {
      renderer.unmount();
    });

    //then
    assert.deepEqual(onMock.times, 3);
    assert.deepEqual(offMock.times, 3);
    assert.deepEqual(capturedOffListeners, [
      ["keypress", keyListener],
      ["wheelup", wheelupListener],
      ["wheeldown", wheeldownListener],
    ]);
  });

  it("should re-subscribe listeners when input element changes", () => {
    //given
    let keyListener = () => {};
    let wheelupListener = () => {};
    let wheeldownListener = () => {};
    const onMock = mockFunction((_, listener) => {
      if (onMock.times === 1) {
        keyListener = listener;
      }
      if (onMock.times === 2) {
        wheelupListener = listener;
      }
      if (onMock.times === 3) {
        wheeldownListener = listener;
      }
    });
    const capturedOffListeners = /** @type {any[]} */ ([]);
    const offMock = mockFunction((n, l) => capturedOffListeners.push([n, l]));
    const props = getViewerInputProps(
      {},
      /** @type {any} */ ({ on: onMock, off: offMock })
    );
    const renderer = TestRenderer.create(h(ViewerInput, props));
    const updatedProps = getViewerInputProps(
      {},
      /** @type {any} */ ({ on: onMock, off: offMock })
    );

    //when
    TestRenderer.act(() => {
      renderer.update(h(ViewerInput, updatedProps));
    });

    //then
    assert.deepEqual(onMock.times, 6);
    assert.deepEqual(offMock.times, 3);
    assert.deepEqual(capturedOffListeners, [
      ["keypress", keyListener],
      ["wheelup", wheelupListener],
      ["wheeldown", wheeldownListener],
    ]);
  });

  it("should render empty component", () => {
    //given
    const props = getViewerInputProps();

    //when
    const result = TestRenderer.create(h(ViewerInput, props)).root;

    //then
    assertViewerInput(result);
  });

  it("should render non-empty component", () => {
    //given
    const props = getViewerInputProps();
    const child = () => {
      return null;
    };

    //when
    const result = TestRenderer.create(h(ViewerInput, props, h(child))).root;

    //then
    assertViewerInput(result, child);
  });
});

/**
 * @param {Partial<ViewerInputProps>} props
 * @param {BlessedElement | null} [inputEl]
 * @returns {ViewerInputProps}
 */
function getViewerInputProps(props = {}, inputEl = null) {
  const inputRef =
    /** @type {React.MutableRefObject<BlessedElement | null>} */ (
      React.createRef()
    );
  inputRef.current = inputEl;
  return {
    inputRef,
    onWheel: mockFunction(),
    onKeypress: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {React.FunctionComponent} [child]
 */
function assertViewerInput(result, child) {
  assert.deepEqual(ViewerInput.displayName, "ViewerInput");

  if (child) {
    assertComponents(result.children, h(child, {}));
  } else {
    assert.deepEqual(result.children, []);
  }
}
