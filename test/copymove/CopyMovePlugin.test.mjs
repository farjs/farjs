/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("@farjs/filelist/api/FileListCapability.mjs").FileListCapability} FileListCapability
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { CopyMoveUiAction } from "../../copymove/CopyMoveUi.mjs"
 */
import React from "react";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import CopyMovePlugin from "../../copymove/CopyMovePlugin.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import FileListEvent from "@farjs/filelist/FileListEvent.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fsComp = () => h("test fsComp");
const otherComp = () => h("test otherComp");

describe("CopyMovePlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["f5", "f6", "S-f5", "S-f6"];

    //when & then
    assert.deepEqual(CopyMovePlugin.instance.triggerKeys, expected);
  });

  it("should return undefined if .. when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([
          FileListCapability.read,
          FileListCapability.write,
          FileListCapability.delete,
          FileListCapability.copyInplace,
          FileListCapability.moveInplace,
        ]),
      }),
    });
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("item 1")],
      },
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

    /** @type {(key: string) => Promise<void>} */
    async function check(key) {
      assert.deepEqual(
        await CopyMovePlugin.instance.onKeyTrigger(key, stacks),
        undefined
      );
    }

    //when & then
    await check("f5");
    await check("f6");
    await check("S-f5");
    await check("S-f6");
  });

  it("should return undefined if other state type when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([
          FileListCapability.read,
          FileListCapability.write,
          FileListCapability.delete,
          FileListCapability.copyInplace,
          FileListCapability.moveInplace,
        ]),
      }),
    });
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(otherComp, dispatch, actions, "otherState")],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(otherComp, dispatch, actions, "otherState")],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    /** @type {(key: string) => Promise<void>} */
    async function check(key) {
      assert.deepEqual(
        await CopyMovePlugin.instance.onKeyTrigger(key, stacks),
        undefined
      );
    }

    //when & then
    await check("f5");
    await check("f6");
    await check("S-f5");
    await check("S-f6");
  });

  it("should return undefined when onKeyTrigger(unknown)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([
          FileListCapability.read,
          FileListCapability.write,
          FileListCapability.delete,
          FileListCapability.copyInplace,
          FileListCapability.moveInplace,
        ]),
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

    //when & then
    assert.deepEqual(
      await CopyMovePlugin.instance.onKeyTrigger("unknown", stacks),
      undefined
    );
  });

  it("should return ui when onKeyTrigger(Shift-F5)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([FileListCapability.copyInplace]),
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
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(otherComp)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when & then
    assert.notDeepEqual(
      await CopyMovePlugin.instance.onKeyTrigger("S-f5", stacks),
      undefined
    );
  });

  it("should return ui when onKeyTrigger(F5)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([
          FileListCapability.read,
          FileListCapability.write,
          FileListCapability.delete,
        ]),
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

    //when & then
    assert.notDeepEqual(
      await CopyMovePlugin.instance.onKeyTrigger("f5", stacks),
      undefined
    );
  });

  it("should return ui if selected items when onKeyTrigger(F6)", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({
        capabilities: new Set([
          FileListCapability.read,
          FileListCapability.write,
          FileListCapability.delete,
        ]),
      }),
    });
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("item 1")],
      },
      selectedNames: new Set(["item 1"]),
    };
    const leftStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when & then
    assert.notDeepEqual(
      await CopyMovePlugin.instance.onKeyTrigger("f6", stacks),
      undefined
    );
  });

  it("should return action when _onCopyMoveInplace", () => {
    //given
    const { _onCopyMoveInplace } = CopyMovePlugin.instance;
    const currState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [
          FileListItem.up,
          FileListItem("file 1"),
          FileListItem("dir 1", true),
        ],
      },
      selectedNames: new Set(["item 1"]),
    };
    const capabilities = [
      FileListCapability.copyInplace,
      FileListCapability.moveInplace,
    ];

    /**
     * @typedef {{
     *  readonly key: string;
     *  readonly action: CopyMoveUiAction;
     *  readonly index: number;
     *  readonly selectedNames: string[];
     *  readonly never: boolean;
     *  readonly capabilities: FileListCapability[];
     * }} CheckParams
     */
    /** @type {CheckParams} */
    const defaults = {
      key: "",
      action: "ShowCopyInplace",
      index: 0,
      selectedNames: [],
      never: false,
      capabilities,
    };

    /** @type {(params: Partial<CheckParams>) => void} */
    function check(params) {
      //given
      const { key, action, index, selectedNames, never, capabilities } = {
        ...defaults,
        ...params,
      };
      const dispatch = mockFunction();
      const actions = new MockFileListActions({
        api: new MockFileListApi({
          capabilities: new Set(capabilities),
        }),
      });
      /** @type {FileListData} */
      const from = {
        dispatch,
        actions,
        state: { ...currState, index, selectedNames: new Set(selectedNames) },
      };

      //when
      const res = _onCopyMoveInplace(key === "S-f6", from);

      //then
      if (!never) {
        assert.deepEqual(res, action);
      } else {
        assert.deepEqual(res, undefined);
      }
    }

    //when & then
    check({ key: "S-f5", action: "ShowCopyInplace", never: true });
    check({
      key: "S-f5",
      action: "ShowCopyInplace",
      never: true,
      selectedNames: ["file 1"],
    });
    check({
      key: "S-f5",
      action: "ShowCopyInplace",
      index: 1,
      never: true,
      capabilities: [FileListCapability.moveInplace],
    });
    check({
      key: "S-f5",
      action: "ShowCopyInplace",
      index: 1,
      capabilities: [FileListCapability.copyInplace],
    });
    check({ key: "S-f5", action: "ShowCopyInplace", index: 2 });

    //when & then
    check({ key: "S-f6", action: "ShowMoveInplace", never: true });
    check({
      key: "S-f6",
      action: "ShowMoveInplace",
      never: true,
      selectedNames: ["file 1"],
    });
    check({
      key: "S-f6",
      action: "ShowMoveInplace",
      index: 1,
      never: true,
      capabilities: [FileListCapability.copyInplace],
    });
    check({
      key: "S-f6",
      action: "ShowMoveInplace",
      index: 1,
      capabilities: [FileListCapability.moveInplace],
    });
    check({ key: "S-f6", action: "ShowMoveInplace", index: 2 });
  });

  it("should return action or emit FileListEvent when _onCopyMove", () => {
    //given
    const { _onCopyMove } = CopyMovePlugin.instance;
    let emitArgs = /** @type {any[]} */ ([]);
    const emitMock = mockFunction((...args) => {
      emitArgs = args;
    });
    const toButton = /** @type {any} */ ({ emit: emitMock });
    const currState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [
          FileListItem.up,
          FileListItem("file 1"),
          FileListItem("dir 1", true),
        ],
      },
      selectedNames: new Set(["item 1"]),
    };
    const capabilities = [
      FileListCapability.read,
      FileListCapability.write,
      FileListCapability.delete,
    ];

    /**
     * @typedef {{
     *  readonly key: string;
     *  readonly action: CopyMoveUiAction;
     *  readonly index: number;
     *  readonly selectedNames: string[];
     *  readonly never: boolean;
     *  readonly fromCapabilities: FileListCapability[];
     *  readonly toCapabilities: FileListCapability[];
     *  readonly event?: import("@farjs/filelist/FileListEvent.mjs").FileListEvent;
     * }} CheckParams
     */
    /** @type {CheckParams} */
    const defaults = {
      key: "",
      action: "ShowCopyInplace",
      index: 0,
      selectedNames: [],
      never: false,
      fromCapabilities: capabilities,
      toCapabilities: capabilities,
    };

    /** @type {(params: Partial<CheckParams>) => void} */
    function check(params) {
      //given
      const {
        key,
        action,
        index,
        selectedNames,
        never,
        fromCapabilities,
        toCapabilities,
        event,
      } = {
        ...defaults,
        ...params,
      };
      const dispatch = mockFunction();
      const fromActions = new MockFileListActions({
        api: new MockFileListApi({
          capabilities: new Set(fromCapabilities),
        }),
      });
      const toActions = new MockFileListActions({
        api: new MockFileListApi({
          capabilities: new Set(toCapabilities),
        }),
      });
      /** @type {FileListData} */
      const from = {
        dispatch,
        actions: fromActions,
        state: { ...currState, index, selectedNames: new Set(selectedNames) },
      };
      /** @type {FileListData} */
      const to = {
        dispatch,
        actions: toActions,
        state: currState,
      };

      //when
      const res = _onCopyMove(key === "f6", from, to, toButton);

      //then
      if (event !== undefined) {
        assert.deepEqual(emitArgs, [
          "keypress",
          undefined,
          {
            name: "",
            full: event,
          },
        ]);
      }
      if (!never) {
        assert.deepEqual(res, action);
      } else {
        assert.deepEqual(res, undefined);
      }
    }

    //when & then
    check({ key: "f5", action: "ShowCopyToTarget", never: true });
    check({
      key: "f5",
      action: "ShowCopyToTarget",
      index: 1,
      never: true,
      fromCapabilities: [],
    });
    check({
      key: "f5",
      action: "ShowCopyToTarget",
      index: 1,
      never: true,
      toCapabilities: [],
      event: FileListEvent.onFileListCopy,
    });
    check({
      key: "f5",
      action: "ShowCopyToTarget",
      index: 1,
      fromCapabilities: [FileListCapability.read],
    });
    check({ key: "f5", action: "ShowCopyToTarget", index: 2 });
    check({ key: "f5", action: "ShowCopyToTarget", selectedNames: ["file 1"] });

    //when & then
    check({ key: "f6", action: "ShowMoveToTarget", never: true });
    check({
      key: "f6",
      action: "ShowMoveToTarget",
      index: 1,
      never: true,
      fromCapabilities: [FileListCapability.read],
    });
    check({
      key: "f6",
      action: "ShowMoveToTarget",
      index: 1,
      never: true,
      toCapabilities: [],
      event: FileListEvent.onFileListMove,
    });
    check({ key: "f6", action: "ShowMoveToTarget", index: 1 });
    check({ key: "f6", action: "ShowMoveToTarget", index: 2 });
    check({ key: "f6", action: "ShowMoveToTarget", selectedNames: ["file 1"] });
  });
});
