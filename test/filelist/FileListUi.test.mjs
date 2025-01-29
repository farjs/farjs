/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("../../filelist/FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import HelpController from "../../filelist/popups/HelpController.mjs";
import ExitController from "../../filelist/popups/ExitController.mjs";
import MenuController from "../../filelist/popups/MenuController.mjs";
import DeleteController from "../../filelist/popups/DeleteController.mjs";
import MakeFolderController from "../../filelist/popups/MakeFolderController.mjs";
import SelectController from "../../filelist/popups/SelectController.mjs";
import FileListUi from "../../filelist/FileListUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FileListUi.helpController = mockComponent(HelpController);
FileListUi.exitController = mockComponent(ExitController);
FileListUi.menuController = mockComponent(MenuController);
FileListUi.deleteController = mockComponent(DeleteController);
FileListUi.makeFolderController = mockComponent(MakeFolderController);
FileListUi.selectController = mockComponent(SelectController);

const {
  helpController,
  exitController,
  menuController,
  deleteController,
  makeFolderController,
  selectController,
} = FileListUi;

describe("FileListUi.test.mjs", () => {
  it("should render component", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = FileListState();
    const onClose = mockFunction();
    const props = { dispatch, onClose };
    const data = FileListData(dispatch, actions, state);
    const fileListUi = FileListUi({
      data,
      onClose: mockFunction(),
    });

    //when
    const result = TestRenderer.create(h(fileListUi, props)).root;

    //then
    /** @type {FileListUiData} */
    const uiData = { data, onClose };
    assertFileListUi(result, fileListUi, uiData);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} fileListUi
 * @param {FileListUiData} uiData
 */
function assertFileListUi(result, fileListUi, uiData) {
  assert.deepEqual(fileListUi.displayName, "FileListUi");

  const resOnClose = result.findByType(helpController).props.onClose;
  assert.deepEqual(resOnClose === uiData.onClose, true);

  assertComponents(
    result.children,
    h(helpController, uiData),
    h(exitController, uiData),
    h(menuController, uiData),
    h(deleteController, uiData),
    h(makeFolderController, uiData),
    h(selectController, uiData)
  );
}
