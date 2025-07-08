/**
 * @import { FoldersHistoryControllerProps } from "../../../fs/popups/FoldersHistoryController.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FoldersHistoryPopup from "../../../fs/popups/FoldersHistoryPopup.mjs";
import FoldersHistoryController from "../../../fs/popups/FoldersHistoryController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FoldersHistoryController.foldersHistoryPopup =
  mockComponent(FoldersHistoryPopup);

const { foldersHistoryPopup } = FoldersHistoryController;

describe("FoldersHistoryController.test.mjs", () => {
  it("should call onChangeDir when onChangeDir", async () => {
    //given
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const onClose = mockFunction();
    const props = getFoldersHistoryControllerProps({ onChangeDir, onClose });

    const renderer = TestRenderer.create(h(FoldersHistoryController, props));

    const popupProps = renderer.root.findByType(foldersHistoryPopup).props;
    const dir = "test dir";

    //when
    popupProps.onChangeDir(dir);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, [dir]);
  });

  it("should render popup component", async () => {
    //given
    const props = getFoldersHistoryControllerProps();

    //when
    const result = TestRenderer.create(h(FoldersHistoryController, props)).root;

    //then
    assertFoldersHistoryController(result, props);
  });

  it("should render empty component", async () => {
    //given
    const props = getFoldersHistoryControllerProps({ showPopup: false });

    //when
    const result = TestRenderer.create(h(FoldersHistoryController, props)).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {Partial<FoldersHistoryControllerProps>} props
 * @returns {FoldersHistoryControllerProps}
 */
function getFoldersHistoryControllerProps(props = {}) {
  return {
    showPopup: true,
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {FoldersHistoryControllerProps} props
 */
function assertFoldersHistoryController(result, props) {
  assert.deepEqual(
    FoldersHistoryController.displayName,
    "FoldersHistoryController"
  );

  const popupProps = result.findByType(foldersHistoryPopup).props;
  assert.deepEqual(popupProps.onClose === props.onClose, true);

  assertComponents(
    result.children,
    h(foldersHistoryPopup, {
      onChangeDir: mockFunction(),
      onClose: popupProps.onClose,
    })
  );
}
