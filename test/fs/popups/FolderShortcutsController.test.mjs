/**
 * @import { FolderShortcutsControllerProps } from "../../../fs/popups/FolderShortcutsController.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FolderShortcutsPopup from "../../../fs/popups/FolderShortcutsPopup.mjs";
import FolderShortcutsController from "../../../fs/popups/FolderShortcutsController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FolderShortcutsController.folderShortcutsPopup =
  mockComponent(FolderShortcutsPopup);

const { folderShortcutsPopup } = FolderShortcutsController;

describe("FolderShortcutsController.test.mjs", () => {
  it("should call onChangeDir when onChangeDir", () => {
    //given
    let onChangeDirArgs = /** @type {any[]} */ ([]);
    const onChangeDir = mockFunction((...args) => (onChangeDirArgs = args));
    const onClose = mockFunction();
    const props = getFolderShortcutsControllerProps({
      showPopup: true,
      onChangeDir,
      onClose,
    });
    const comp = TestRenderer.create(h(FolderShortcutsController, props)).root;
    const popupProps = comp.findByType(folderShortcutsPopup).props;
    const dir = "test dir";

    //when
    popupProps.onChangeDir(dir);

    //then
    assert.deepEqual(onChangeDir.times, 1);
    assert.deepEqual(onChangeDirArgs, [dir]);
    assert.deepEqual(onClose.times, 1);
  });

  it("should call onClose when onClose", () => {
    //given
    const onClose = mockFunction();
    const props = getFolderShortcutsControllerProps({
      showPopup: true,
      onClose,
    });
    const comp = TestRenderer.create(h(FolderShortcutsController, props)).root;
    const popupProps = comp.findByType(folderShortcutsPopup).props;

    //when
    popupProps.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const props = getFolderShortcutsControllerProps({ showPopup: true });

    //when
    const result = TestRenderer.create(
      h(FolderShortcutsController, props)
    ).root;

    //then
    assertFolderShortcutsController(result);
  });

  it("should render empty component", () => {
    //given
    const props = getFolderShortcutsControllerProps();

    //when
    const result = TestRenderer.create(
      h(FolderShortcutsController, props)
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {Partial<FolderShortcutsControllerProps>} props
 * @returns {FolderShortcutsControllerProps}
 */
function getFolderShortcutsControllerProps(props = {}) {
  return {
    showPopup: false,
    onChangeDir: mockFunction(),
    onClose: mockFunction(),
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertFolderShortcutsController(result) {
  assert.deepEqual(
    FolderShortcutsController.displayName,
    "FolderShortcutsController"
  );

  assertComponents(
    result.children,
    h(folderShortcutsPopup, {
      onChangeDir: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
