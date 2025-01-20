import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import MenuBar from "@farjs/ui/menu/MenuBar.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import MenuController from "../../../filelist/popups/MenuController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

MenuController.menuBarComp = mockComponent(MenuBar);

const { menuBarComp } = MenuController;

describe("MenuController.test.mjs", () => {
  it("should emit keypress event globally when onAction", () => {
    //given
    const onClose = mockFunction();
    let keyListenerArgs = /** @type {any[]} */ ([]);
    const keyListener = mockFunction((...args) => (keyListenerArgs = args));
    const props = { onClose, showMenuPopup: true };
    const comp = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;
    const menuBarProps = comp.findByType(menuBarComp).props;
    process.stdin.on("keypress", keyListener);

    //when
    menuBarProps.onAction(1, 0);

    //cleanup
    process.stdin.removeListener("keypress", keyListener);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(keyListener.times, 1);
    assert.deepEqual(keyListenerArgs, [
      undefined,
      {
        name: "f3",
        full: "f3",
        ctrl: false,
        meta: false,
        shift: false,
      },
    ]);
  });

  it("should emit keypress event for left panel when onAction", () => {
    //given
    const onClose = mockFunction();
    let emitterArgs = /** @type {any[]} */ ([]);
    const emitter = mockFunction((...args) => (emitterArgs = args));
    const props = { onClose, showMenuPopup: true };
    const leftInput = /** @type {any} */ ({ emit: emitter });
    const comp = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(
          new PanelStack(true, [], mockFunction()),
          leftInput
        ),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;
    const menuBarProps = comp.findByType(menuBarComp).props;

    //when
    menuBarProps.onAction(0, 4);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(emitter.times, 1);
    assert.deepEqual(emitterArgs, [
      "keypress",
      undefined,
      {
        name: "l",
        full: "M-l",
        ctrl: false,
        meta: true,
        shift: false,
      },
    ]);
  });

  it("should emit keypress event for right panel when onAction", () => {
    //given
    const onClose = mockFunction();
    let emitterArgs = /** @type {any[]} */ ([]);
    const emitter = mockFunction((...args) => (emitterArgs = args));
    const props = { onClose, showMenuPopup: true };
    const rightInput = /** @type {any} */ ({ emit: emitter });
    const comp = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(
          new PanelStack(false, [], mockFunction()),
          rightInput
        ),
      })
    ).root;
    const menuBarProps = comp.findByType(menuBarComp).props;

    //when
    menuBarProps.onAction(4, 4);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(emitter.times, 1);
    assert.deepEqual(emitterArgs, [
      "keypress",
      undefined,
      {
        name: "r",
        full: "M-r",
        ctrl: false,
        meta: true,
        shift: false,
      },
    ]);
  });

  it("should call onClose when onClose", () => {
    //given
    const onClose = mockFunction();
    const props = { onClose, showMenuPopup: true };
    const comp = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;
    const menuBarProps = comp.findByType(menuBarComp).props;

    //when
    menuBarProps.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render MenuBar component", () => {
    //given
    const props = { onClose: mockFunction(), showMenuPopup: true };

    //when
    const result = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;

    //then
    assertMenuController(result);
  });

  it("should render empty component", () => {
    //given
    const props = { onClose: mockFunction() };

    //when
    const result = TestRenderer.create(
      withStacksContext(h(MenuController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertMenuController(result) {
  assert.deepEqual(MenuController.displayName, "MenuController");

  assertComponents(
    result.children,
    h(menuBarComp, {
      items: MenuController._items,
      onAction: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
