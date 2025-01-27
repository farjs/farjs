/**
 * @typedef {import("@farjs/filelist/FileListActions.mjs").FileListAction} FileListAction
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListData from "@farjs/filelist/FileListData.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import MockHistoryProvider from "@farjs/filelist/history/MockHistoryProvider.mjs";
import withHistoryProvider from "@farjs/filelist/history/withHistoryProvider.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import MockHistoryService from "@farjs/filelist/history/MockHistoryService.mjs";
import SelectPopup from "../../../filelist/popups/SelectPopup.mjs";
import SelectController from "../../../filelist/popups/SelectController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

SelectController.selectPopupComp = mockComponent(SelectPopup);

const { selectPopupComp } = SelectController;

describe("SelectController.test.mjs", () => {
  it("should not select .. when onAction", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions();
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3"),
    ]);
    const state = {
      ...FileListState(),
      offset: 1,
      index: 2,
      currDir,
      selectedNames: new Set(["file.test3"]),
    };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showSelectPopup: true };

    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    );
    const popupProps = renderer.root.findByType(selectPopupComp).props;
    assert.deepEqual(popupProps.showSelect, true);
    const pattern = "*";

    /** @type {FileListAction} */
    const action = {
      action: "FileListParamsChangedAction",
      offset: state.offset,
      index: state.index,
      selectedNames: new Set(["file1.test", "file2.test", "file.test3"]),
    };

    //when
    popupProps.onAction(pattern);

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: pattern }]);

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
  });

  it("should not dispatch action if same selection", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3"),
    ]);
    const state = {
      ...FileListState(),
      offset: 1,
      index: 2,
      currDir,
      selectedNames: new Set(["file1.test", "file2.test", "file.test3"]),
    };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showSelectPopup: true };

    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    );
    const popupProps = renderer.root.findByType(selectPopupComp).props;
    assert.deepEqual(popupProps.showSelect, true);
    const pattern = "*";

    //when
    popupProps.onAction(pattern);

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: pattern }]);

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 0);
  });

  it("should dispatch action and save history when onAction and Select", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions();
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3"),
    ]);
    const state = {
      ...FileListState(),
      offset: 1,
      index: 2,
      currDir,
      selectedNames: new Set(["file.test3"]),
    };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showSelectPopup: true };

    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    );
    const popupProps = renderer.root.findByType(selectPopupComp).props;
    assert.deepEqual(popupProps.showSelect, true);
    const pattern = "*.test";

    /** @type {FileListAction} */
    const action = {
      action: "FileListParamsChangedAction",
      offset: state.offset,
      index: state.index,
      selectedNames: new Set(["file1.test", "file2.test", "file.test3"]),
    };

    //when
    popupProps.onAction(pattern);

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: pattern }]);

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
  });

  it("should dispatch action and save history when onAction and Deselect", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => (dispatchArgs = args));
    const actions = new MockFileListActions();
    const currDir = FileListDir("/sub-dir", false, [
      FileListItem.up,
      FileListItem("file1.test"),
      FileListItem("file2.test"),
      FileListItem("file.test3"),
    ]);
    const state = {
      ...FileListState(),
      offset: 1,
      index: 2,
      currDir,
      selectedNames: new Set(["file1.test", "file2.test", "file.test3"]),
    };
    const data = FileListData(dispatch, actions, state);
    const onClose = mockFunction();
    const props = { onClose, data, showSelectPopup: false };

    let getArgs = /** @type {any[]} */ ([]);
    const get = mockFunction((...args) => {
      getArgs = args;
      return getP;
    });
    let saveArgs = /** @type {any[]} */ ([]);
    const save = mockFunction((...args) => {
      saveArgs = args;
      return saveP;
    });
    const service = new MockHistoryService({ save });
    const historyProvider = new MockHistoryProvider({ get });
    const getP = Promise.resolve(service);
    const saveP = Promise.resolve();

    const renderer = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    );
    const popupProps = renderer.root.findByType(selectPopupComp).props;
    assert.deepEqual(popupProps.showSelect, false);
    const pattern = "file1.test;file2.test";

    /** @type {FileListAction} */
    const action = {
      action: "FileListParamsChangedAction",
      offset: state.offset,
      index: state.index,
      selectedNames: new Set(["file.test3"]),
    };

    //when
    popupProps.onAction(pattern);

    //then
    await getP;
    await saveP;
    assert.deepEqual(get.times, 1);
    assert.deepEqual(getArgs, [SelectPopup.selectPatternsHistoryKind]);
    assert.deepEqual(save.times, 1);
    assert.deepEqual(saveArgs, [{ item: pattern }]);

    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(dispatch.times, 1);
    assert.deepEqual(dispatchArgs, [action]);
  });

  it("should call onClose when onCancel", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const onClose = mockFunction();
    const props = { onClose, data, showSelectPopup: true };
    const historyProvider = new MockHistoryProvider();
    const comp = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    ).root;
    const popupProps = comp.findByType(selectPopupComp).props;

    //when
    popupProps.onCancel();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render Select popup", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data, showSelectPopup: true };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    ).root;

    //then
    assertSelectController(result, true);
  });

  it("should render Deselect popup", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data, showSelectPopup: false };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    ).root;

    //then
    assertSelectController(result, false);
  });

  it("should render empty component when showSelectPopup is undefined", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const data = FileListData(dispatch, actions, FileListState());
    const props = { onClose: mockFunction(), data };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });

  it("should render empty component when data is undefined", () => {
    //given
    const props = { onClose: mockFunction(), showSelectPopup: true };
    const historyProvider = new MockHistoryProvider();

    //when
    const result = TestRenderer.create(
      withHistoryProvider(h(SelectController, props), historyProvider)
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });

  it("should escape special chars when _fileMaskToRegex", () => {
    //when & then
    assert.deepEqual(
      SelectController._fileMaskToRegex("aa\\.()[]{}+-^$!*?bb"),
      "^aa\\\\\\.\\(\\)\\[\\]\\{\\}\\+\\-\\^\\$!.*?.bb$"
    );
  });

  it("should match against simple file mask", () => {
    //given
    /** @type {(name: string, mask: string, expected: boolean) => void} */
    function check(name, mask, expected) {
      const regex = SelectController._fileMaskToRegex(mask);

      //when
      const res = new RegExp(regex).test(name);

      //then
      assert.deepEqual(
        res,
        expected,
        `${res} != ${expected}, name: ${name}, regex: ${regex}`
      );
    }

    //when & then
    check("file.name", "file.NAME", false);
    check("file.NAME", "file.NAME", true);
    check("file.name", "file.nam", false);
    check("file.s", "file.??", false);
    check("file.s", "file*", true);
    check("file.", "file?", true);
    check("file.same", "file.??ame", false);
    check("file.ssame", "file.?ame", false);
    check("file.ssame", "file.??ame", true);
    check("file.same", "file.?ame", true);
    check("file.name", "file.name", true);
    check("^file$.name", "^file$.name", true);
    check("file()[]{}+-!.name", "file()[]{}+-!.name", true);
    check(".name", "*.name", true);
    check("file.name", "*.name", true);
    check("file.name", "*.*", true);
    check("file.na.me", "*.na.*", true);
    check("file.na.me", "*", true);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {boolean} showSelect
 */
function assertSelectController(result, showSelect) {
  assert.deepEqual(SelectController.displayName, "SelectController");

  assertComponents(
    result.children,
    h(selectPopupComp, {
      showSelect,
      onAction: mockFunction(),
      onCancel: mockFunction(),
    })
  );
}
