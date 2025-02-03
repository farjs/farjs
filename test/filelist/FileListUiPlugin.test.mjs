/**
 * @typedef {import("../../filelist/FileListUi.mjs").FileListUiData} FileListUiData
 */
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import FileListUiPlugin from "../../filelist/FileListUiPlugin.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("FileListUiPlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"];

    //when & then
    assert.deepEqual(FileListUiPlugin.triggerKeys, expected);
  });

  it("should return undefined if non-trigger key when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1")],
      },
    };
    const fsComp = () => {
      return null;
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await FileListUiPlugin.onKeyTrigger("test_key", stacks);

    //then
    assert.deepEqual(result, undefined);
  });

  it("should return ui component if trigger key when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1")],
      },
    };
    const fsComp = () => {
      return null;
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await FileListUiPlugin.onKeyTrigger("f1", stacks);

    //then
    assert.deepEqual(result !== undefined, true);
  });

  it("should return ui data if trigger key=f1/f9/f10/Alt-S/Alt-D when _createUiData", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up],
      },
    };
    const data = /** @type {FileListData} */ { dispatch, actions, state };

    /**
     * @param {string} key
     * @param {Partial<FileListUiData>} partial
     */
    function check(key, partial) {
      //when
      const result = FileListUiPlugin._createUiData(key, data);

      //then
      assert.deepEqual(result, {
        ...partial,
        onClose: result?.onClose,
        data,
      });
    }

    //when & then
    check("f1", { showHelpPopup: true });
    check("f9", { showMenuPopup: true });
    check("f10", { showExitPopup: true });
    check("M-s", { showSelectPopup: true });
    check("M-d", { showSelectPopup: false });
  });

  it("should handle trigger key (f7) when _createUiData", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([FileListCapability.mkDirs]),
      }),
    });
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up],
      },
    };
    const data = /** @type {FileListData} */ { dispatch, actions, state };
    const noCapabilityData = /** @type {FileListData} */ {
      ...data,
      actions: new MockFileListActions(),
    };

    //when & then
    assert.deepEqual(
      FileListUiPlugin._createUiData("f7", undefined),
      undefined
    );
    assert.deepEqual(
      FileListUiPlugin._createUiData("f7", noCapabilityData),
      undefined
    );

    //when
    const result = FileListUiPlugin._createUiData("f7", data);

    //then
    assert.deepEqual(result, {
      showMkFolderPopup: true,
      onClose: result?.onClose,
      data,
    });
  });

  it("should handle trigger keys (f8/delete) when _createUiData", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([FileListCapability.delete]),
      }),
    });
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1")],
      },
    };
    const data = /** @type {FileListData} */ { dispatch, actions, state };
    const noCapabilityData = /** @type {FileListData} */ {
      ...data,
      actions: new MockFileListActions(),
    };
    const noItemData = /** @type {FileListData} */ {
      ...data,
      state: {
        ...state,
        currDir: {
          ...state.currDir,
          items: [FileListItem(".."), FileListItem("item 1")],
        },
      },
    };
    const selectedItemsData = /** @type {FileListData} */ {
      ...data,
      state: {
        ...state,
        currDir: {
          ...state.currDir,
          items: [FileListItem.up, FileListItem("test")],
        },
        selectedNames: new Set(["test"]),
      },
    };

    //when & then
    assert.deepEqual(
      FileListUiPlugin._createUiData("f8", undefined),
      undefined
    );
    assert.deepEqual(
      FileListUiPlugin._createUiData("f8", noCapabilityData),
      undefined
    );
    assert.deepEqual(
      FileListUiPlugin._createUiData("f8", noItemData),
      undefined
    );

    //when & then
    const result1 = FileListUiPlugin._createUiData("f8", selectedItemsData);
    assert.deepEqual(result1, {
      showDeletePopup: true,
      onClose: result1?.onClose,
      data: selectedItemsData,
    });

    //when & then
    const result2 = FileListUiPlugin._createUiData("delete", data);
    assert.deepEqual(result2, {
      showDeletePopup: true,
      onClose: result2?.onClose,
      data,
    });
  });
});
