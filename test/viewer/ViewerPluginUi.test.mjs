/**
 * @typedef {import("@farjs/filelist/FileListData.mjs").ReactComponent} ReactComponent
 * @typedef {import("@farjs/filelist/FileListPlugin.mjs").FileListPluginUiProps} FileListPluginUiProps
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import Popup from "@farjs/ui/popup/Popup.mjs";
import BottomMenu from "@farjs/ui/menu/BottomMenu.mjs";
import { createViewerFileViewport } from "../../viewer/ViewerFileViewport.mjs";
import MockViewerFileReader from "../../viewer/MockViewerFileReader.mjs";
import ViewerHeader from "../../viewer/ViewerHeader.mjs";
import ViewerFileLine from "../../viewer/ViewerFileLine.mjs";
import ViewerController from "../../viewer/ViewerController.mjs";
import ViewerPluginUi from "../../viewer/ViewerPluginUi.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

ViewerPluginUi.popupComp = mockComponent(Popup);
ViewerPluginUi.viewerHeader = mockComponent(ViewerHeader);
ViewerPluginUi.viewerController = mockComponent(ViewerController);
ViewerPluginUi.bottomMenuComp = mockComponent(BottomMenu);

const {
  popupComp,
  viewerHeader,
  viewerController,
  bottomMenuComp,
  defaultMenuItems,
} = ViewerPluginUi;

describe("ViewerPluginUi.test.mjs", () => {
  it("should call onClose when onClose", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const pluginUi = ViewerPluginUi("item 1", 123);
    const props = { dispatch, onClose };
    const renderer = TestRenderer.create(h(pluginUi, props));

    //when
    renderer.root.findByType(popupComp).props.onClose();

    //then
    assert.deepEqual(onClose.times, 1);
  });

  it("should call onClose when onKeypress(F3)", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const pluginUi = ViewerPluginUi("item 1", 123);
    const props = { dispatch, onClose };
    const renderer = TestRenderer.create(h(pluginUi, props));

    //when
    const result = renderer.root.findByType(popupComp).props.onKeypress("f3");

    //then
    assert.deepEqual(result, true);
    assert.deepEqual(onClose.times, 1);
  });

  it("should call onClose when onKeypress(F10)", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const pluginUi = ViewerPluginUi("item 1", 123);
    const props = { dispatch, onClose };
    const renderer = TestRenderer.create(h(pluginUi, props));

    //when
    const result = renderer.root.findByType(popupComp).props.onKeypress("f10");

    //then
    assert.deepEqual(result, true);
    assert.deepEqual(onClose.times, 1);
  });

  it("should do nothing when onKeypress(unknown)", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const pluginUi = ViewerPluginUi("item 1", 123);
    const props = { dispatch, onClose };
    const renderer = TestRenderer.create(h(pluginUi, props));

    //when
    const result = renderer.root
      .findByType(popupComp)
      .props.onKeypress("unknown");

    //then
    assert.deepEqual(result, false);
    assert.deepEqual(onClose.times, 0);
  });

  it("should update props when setViewport", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const pluginUi = ViewerPluginUi("item 1", 0);
    const props = { dispatch, onClose };
    const renderer = TestRenderer.create(h(pluginUi, props));
    const viewport = createViewerFileViewport(
      new MockViewerFileReader(),
      "utf-8",
      110,
      1,
      2,
      true,
      3,
      undefined,
      [ViewerFileLine("test...", 55)]
    );

    //when
    renderer.root.findByType(viewerController).props.setViewport(viewport);

    //then
    const headerProps = renderer.root.findByType(viewerHeader).props;
    assert.deepEqual(headerProps, {
      filePath: "item 1",
      encoding: viewport.encoding,
      size: viewport.size,
      column: viewport.column,
      percent: 50,
    });
    assert.deepEqual(
      renderer.root.findByType(viewerController).props.viewport === viewport,
      true
    );
    const menuItems = [...defaultMenuItems];
    menuItems[1] = "Unwrap";
    assert.deepEqual(
      renderer.root.findByType(bottomMenuComp).props.items,
      menuItems
    );
  });

  it("should render initial component", () => {
    //given
    const dispatch = mockFunction();
    const onClose = mockFunction();
    const filePath = "item 1";
    const size = 123;
    const pluginUi = ViewerPluginUi(filePath, size);
    const props = { dispatch, onClose };
    const inputMock = {};

    //when
    const result = TestRenderer.create(h(pluginUi, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? inputMock : null;
      },
    }).root;

    //then
    assertViewerPluginUi(result, pluginUi, props, filePath, size);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} pluginUi
 * @param {FileListPluginUiProps} props
 * @param {string} filePath
 * @param {number} size
 */
function assertViewerPluginUi(result, pluginUi, props, filePath, size) {
  assert.deepEqual(pluginUi.displayName, "ViewerPluginUi");

  assert.deepEqual(
    result.findByType(popupComp).props.onClose === props.onClose,
    true
  );
  const inputRef = result.findByType(viewerController).props.inputRef;
  assert.deepEqual(inputRef.current, {});

  assertComponents(
    result.children,
    h(
      popupComp,
      { onClose: mockFunction(), onKeypress: mockFunction() },
      h(
        "box",
        {
          clickable: true,
          autoFocus: false,
        },
        h(viewerHeader, {
          filePath,
          encoding: "",
          size: 0,
          column: 0,
          percent: 0,
        }),

        h(
          "button",
          {
            top: 1,
            width: "100%",
            height: "100%-2",
          },
          h(viewerController, {
            inputRef,
            dispatch: props.dispatch,
            filePath,
            size,
            setViewport: mockFunction(),
            onKeypress: mockFunction(),
          })
        ),

        h(
          "box",
          { top: "100%-1" },
          h(bottomMenuComp, { items: defaultMenuItems })
        )
      )
    )
  );
}
