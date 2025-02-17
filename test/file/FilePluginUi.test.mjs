/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileViewHistoryController from "../../file/popups/FileViewHistoryController.mjs";
import FilePluginUi from "../../file/FilePluginUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FilePluginUi.fileViewHistory = mockComponent(FileViewHistoryController);

const { fileViewHistory } = FilePluginUi;

describe("FilePluginUi.test.mjs", () => {
  it("should render component", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const fileUi = FilePluginUi({
      showFileViewHistoryPopup: true,
    });

    //when
    const result = TestRenderer.create(h(fileUi, props)).root;

    //then
    assertFilePluginUi(result, fileUi, props);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} fileUi
 * @param {FileListPluginUiProps} props
 */
function assertFilePluginUi(result, fileUi, props) {
  assert.deepEqual(fileUi.displayName, "FilePluginUi");

  assert.deepEqual(
    result.findByType(fileViewHistory).props.onClose === props.onClose,
    true
  );

  assertComponents(
    result.children,
    h(fileViewHistory, { showPopup: true, onClose: mockFunction() })
  );
}
