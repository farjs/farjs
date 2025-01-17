import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import ExitController from "../../../filelist/popups/ExitController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ExitController.messageBoxComp = mockComponent(MessageBox);

const { messageBoxComp } = ExitController;

describe("ExitController.test.mjs", () => {
  it("should call onClose and emit Ctrl+e when YES action", () => {
    //given
    const onClose = mockFunction();
    let keyListenerArgs = /** @type {any[]} */ ([]);
    const keyListener = mockFunction((...args) => (keyListenerArgs = args));
    const props = { onClose, showExitPopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(ExitController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;
    process.stdin.on("keypress", keyListener);

    //when
    msgBoxProps.actions[0].onAction();

    //cleanup
    process.stdin.removeListener("keypress", keyListener);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(keyListener.times, 1);
    assert.deepEqual(keyListenerArgs, [
      undefined,
      {
        name: "e",
        ctrl: true,
        meta: false,
        shift: false,
      },
    ]);
  });

  it("should call onClose when NO action", () => {
    //given
    const onClose = mockFunction();
    const props = { onClose, showExitPopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(ExitController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;

    //when
    msgBoxProps.actions[1].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const props = { onClose: mockFunction(), showExitPopup: true };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(ExitController, props))
    ).root;

    //then
    assertExitController(result);
  });

  it("should render empty component", () => {
    //given
    const props = { onClose: mockFunction() };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(ExitController, props))
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertExitController(result) {
  assert.deepEqual(ExitController.displayName, "ExitController");

  const currTheme = FileListTheme.defaultTheme;

  assertComponents(
    result.children,
    h(messageBoxComp, {
      title: "Exit",
      message: "Do you really want to exit FAR.js?",
      actions: [
        MessageBoxAction.YES(mockFunction()),
        MessageBoxAction.NO(mockFunction()),
      ],
      style: currTheme.popup.regular,
    })
  );
}
