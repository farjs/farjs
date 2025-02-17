/**
 * @typedef {import("../../../file/FileViewHistory.mjs").FileViewHistory} FileViewHistory
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import FileEvent from "../../../file/FileEvent.mjs";
import FileViewHistoryPopup from "../../../file/popups/FileViewHistoryPopup.mjs";
import FileViewHistoryController from "../../../file/popups/FileViewHistoryController.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FileViewHistoryController.fileViewHistoryPopup =
  mockComponent(FileViewHistoryPopup);

const { fileViewHistoryPopup } = FileViewHistoryController;

describe("FileViewHistoryController.test.mjs", () => {
  it("should emit onFileView event when onAction", () => {
    //given
    const onClose = mockFunction();
    let emitArgs = /** @type {any[]} */ ([]);
    const emit = mockFunction((...args) => (emitArgs = args));
    const props = { onClose, showPopup: true };
    const comp = TestRenderer.create(
      withStacksContext(h(FileViewHistoryController, props), {
        left: WithStacksData(
          new PanelStack(true, [], mockFunction()),
          /** @type {any} */ ({ emit })
        ),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;
    const popupProps = comp.findByType(fileViewHistoryPopup).props;
    /** @type {FileViewHistory} */
    const history = {
      path: "test/path",
      params: {
        isEdit: false,
        encoding: "utf8",
        position: 0,
      },
    };

    //when
    popupProps.onAction(history);

    //then
    assert.deepEqual(onClose.times, 1);
    assert.deepEqual(emit.times, 1);
    assert.deepEqual(emitArgs, [
      "keypress",
      undefined,
      {
        name: "",
        full: FileEvent.onFileView,
        data: history,
      },
    ]);
  });

  it("should call onClose when onClose", () => {
    //given
    const onClose = mockFunction();
    const props = { onClose, showPopup: true };
    const comp = TestRenderer.create(
      withStacksContext(h(FileViewHistoryController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;
    const popupProps = comp.findByType(fileViewHistoryPopup).props;

    //when
    popupProps.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should render popup component", () => {
    //given
    const props = { onClose: mockFunction(), showPopup: true };

    //when
    const result = TestRenderer.create(
      withStacksContext(h(FileViewHistoryController, props), {
        left: WithStacksData(new PanelStack(true, [], mockFunction())),
        right: WithStacksData(new PanelStack(false, [], mockFunction())),
      })
    ).root;

    //then
    assertFileViewHistoryController(result);
  });

  it("should render empty component", () => {
    //given
    const props = { onClose: mockFunction(), showPopup: false };

    //when
    const result = TestRenderer.create(
      withStacksContext(h(FileViewHistoryController, props), {
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
function assertFileViewHistoryController(result) {
  assert.deepEqual(
    FileViewHistoryController.displayName,
    "FileViewHistoryController"
  );

  assertComponents(
    result.children,
    h(fileViewHistoryPopup, {
      onAction: mockFunction(),
      onClose: mockFunction(),
    })
  );
}
