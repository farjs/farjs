/**
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { ArchiverPluginUiParams } from "../../archiver/ArchiverPluginUi.mjs"
 */
import React from "react";
import { deepEqual } from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListApi from "@farjs/filelist/api/MockFileListApi.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import ZipApi from "../../archiver/zip/ZipApi.mjs";
import ZipActions from "../../archiver/zip/ZipActions.mjs";
import ArchiverPlugin from "../../archiver/ArchiverPlugin.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

// replace potential mocks from prev tests with default impl
ZipActions.readZip = () => Promise.resolve(new Map());
ZipActions.createApi = (zipPath, rootPath, entriesByParentF) =>
  new ZipApi(zipPath, rootPath, entriesByParentF);

const plugin = ArchiverPlugin.instance;
const fsComp = () => h("test stub");

describe("ArchiverPlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["S-f7"];

    //when & then
    deepEqual(plugin.triggerKeys, expected);
  });

  it("should return undefined if .. when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("item 1")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when & then
    deepEqual(await plugin.onKeyTrigger("", stacks), undefined);
  });

  it("should return undefined if non-local fs when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions({
      api: new MockFileListApi({ isLocal: false }),
    });
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when & then
    deepEqual(await plugin.onKeyTrigger("", stacks), undefined);
  });

  it("should return ui if not on .. when onKeyTrigger", async () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const leftState = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem("item 1"), FileListItem("item 2")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, leftState)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when & then
    deepEqual((await plugin.onKeyTrigger("", stacks)) !== undefined, true);
  });

  it("should return params if not on .. when _createUiParams", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 1");
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [item, FileListItem("item 2")],
      },
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when
    const result = plugin._createUiParams(stacks);

    //then
    /** @type {ArchiverPluginUiParams} */
    const expected = {
      data: { dispatch, actions, state },
      archName: "item 1.zip",
      archType: "zip",
      addToArchApi: ZipApi.addToZip,
      items: [item],
    };
    deepEqual(result, expected);
  });

  it("should return params if selected items when _createUiParams", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const item = FileListItem("item 2");
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [FileListItem.up, FileListItem("item 1"), item],
      },
      selectedNames: new Set(["item 2"]),
    };
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, dispatch, actions, state)],
      mockFunction(),
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp)],
      mockFunction(),
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack),
    );

    //when
    const result = plugin._createUiParams(stacks);

    //then
    /** @type {ArchiverPluginUiParams} */
    const expected = {
      data: { dispatch, actions, state },
      archName: "item 2.zip",
      archType: "zip",
      addToArchApi: ZipApi.addToZip,
      items: [item],
    };
    deepEqual(result, expected);
  });

  it("should trigger plugin on .zip and .jar file extensions", async () => {
    //given
    const onClose = mockFunction();
    const header = new Uint8Array(5);

    /** @type {(fileName: string) => Promise<PanelStackItem<FileListState> | undefined>} */
    function check(fileName) {
      return plugin.onFileTrigger(fileName, header, onClose);
    }

    //when & then
    deepEqual(await check("filePath.txt"), undefined);
    deepEqual((await check("filePath.zip")) !== undefined, true);
    deepEqual((await check("filePath.ZIP")) !== undefined, true);
    deepEqual((await check("filePath.jar")) !== undefined, true);
    deepEqual((await check("filePath.Jar")) !== undefined, true);
  });

  it("should trigger plugin on PK34 file header", async () => {
    //given
    const onClose = mockFunction();
    const header = new Uint8Array([
      "P".charCodeAt(0),
      "K".charCodeAt(0),
      0x03,
      0x04,
      0x01,
    ]);

    //when & then
    deepEqual(
      await plugin.onFileTrigger("filePath.txt", new Uint8Array(2), onClose),
      undefined,
    );
    deepEqual(
      (await plugin.onFileTrigger("filePath.txt", header, onClose)) !==
        undefined,
      true,
    );
  });
});
