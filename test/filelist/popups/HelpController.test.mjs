import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import HelpController from "../../../filelist/popups/HelpController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

HelpController.messageBoxComp = mockComponent(MessageBox);

const { messageBoxComp } = HelpController;

describe("HelpController.test.mjs", () => {
  it("should call onClose when OK action", () => {
    //given
    const onClose = mockFunction();
    const props = { onClose, showHelpPopup: true };
    const comp = TestRenderer.create(
      withThemeContext(h(HelpController, props))
    ).root;
    const msgBoxProps = comp.findByType(messageBoxComp).props;

    //when
    msgBoxProps.actions[0].onAction();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const props = { onClose: mockFunction(), showHelpPopup: true };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(HelpController, props))
    ).root;

    //then
    assertHelpController(result);
  });

  it("should render empty component", () => {
    //given
    const props = { onClose: mockFunction() };

    //when
    const result = TestRenderer.create(
      withThemeContext(h(HelpController, props))
    ).root;

    //then
    assert.deepEqual(result.children.length, 0);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 */
function assertHelpController(result) {
  assert.deepEqual(HelpController.displayName, "HelpController");

  const currTheme = FileListTheme.defaultTheme;

  assertComponents(
    result.children,
    h(messageBoxComp, {
      title: "Help",
      message: "//TODO: show help/about info",
      actions: [MessageBoxAction.OK(mockFunction())],
      style: currTheme.popup.regular,
    })
  );
}
