/**
 * @import { QuickViewParams } from "../../../viewer/quickview/QuickViewDir.mjs";
 */
import React from "react";
import assert from "node:assert/strict";
import mockFunction from "mock-fn";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import QuickViewPanel from "../../../viewer/quickview/QuickViewPanel.mjs";
import QuickViewPlugin from "../../../viewer/quickview/QuickViewPlugin.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

const fsComp = () => h("test stub");

/** @type {QuickViewParams} */
const params = {
  name: "",
  parent: "",
  folders: 0,
  files: 0,
  filesSize: 0,
};

describe("QuickViewPlugin.test.mjs", () => {
  it("should define triggerKeys", () => {
    //given
    const expected = ["C-q"];

    //when & then
    assert.deepEqual(QuickViewPlugin.triggerKeys, expected);
  });

  it("should remove plugin from left panel when onKeyTrigger", async () => {
    //given
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(QuickViewPanel, undefined, undefined, params),
      new PanelStackItem(fsComp, undefined, undefined, undefined),
    ];
    const leftStack = new PanelStack(
      true,
      stackState,
      (f) => (stackState = f(stackState))
    );
    const rightStack = new PanelStack(
      false,
      [new PanelStackItem(fsComp, undefined, undefined, undefined)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await QuickViewPlugin.onKeyTrigger("", stacks);

    //then
    assert.deepEqual(result, undefined);
    assert.deepEqual(stackState.length, 1);
    assert.deepEqual(stackState[0].component === fsComp, true);
  });

  it("should remove plugin from right panel when onKeyTrigger", async () => {
    //given
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(QuickViewPanel, undefined, undefined, params),
      new PanelStackItem(fsComp, undefined, undefined, undefined),
    ];
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, undefined, undefined, undefined)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      stackState,
      (f) => (stackState = f(stackState))
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await QuickViewPlugin.onKeyTrigger("", stacks);

    //then
    assert.deepEqual(result, undefined);
    assert.deepEqual(stackState.length, 1);
    assert.deepEqual(stackState[0].component === fsComp, true);
  });

  it("should add plugin to left panel when onKeyTrigger", async () => {
    //given
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(fsComp, undefined, undefined, undefined),
    ];
    const leftStack = new PanelStack(
      false,
      stackState,
      (f) => (stackState = f(stackState))
    );
    const rightStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, undefined, undefined, undefined)],
      mockFunction()
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await QuickViewPlugin.onKeyTrigger("", stacks);

    //then
    assert.deepEqual(result, undefined);
    assert.deepEqual(stackState.length, 2);
    assert.deepEqual(stackState[0].component === QuickViewPanel, true);
    assert.deepEqual(stackState[0].state, params);
  });

  it("should add plugin to right panel when onKeyTrigger", async () => {
    //given
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(fsComp, undefined, undefined, undefined),
    ];
    const leftStack = new PanelStack(
      true,
      [new PanelStackItem(fsComp, undefined, undefined, undefined)],
      mockFunction()
    );
    const rightStack = new PanelStack(
      false,
      stackState,
      (f) => (stackState = f(stackState))
    );
    const stacks = WithStacksProps(
      WithStacksData(leftStack),
      WithStacksData(rightStack)
    );

    //when
    const result = await QuickViewPlugin.onKeyTrigger("", stacks);

    //then
    assert.deepEqual(result, undefined);
    assert.deepEqual(stackState.length, 2);
    assert.deepEqual(stackState[0].component === QuickViewPanel, true);
    assert.deepEqual(stackState[0].state, params);
  });
});
