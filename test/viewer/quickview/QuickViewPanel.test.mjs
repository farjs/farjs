/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { WithStackProps } from "@farjs/filelist/stack/WithStack.mjs";
 * @import { QuickViewParams } from "../../../viewer/quickview/QuickViewDir.mjs";
 */
import path from "path";
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import DoubleChars from "@farjs/ui/border/DoubleChars.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import DoubleBorder from "@farjs/ui/border/DoubleBorder.mjs";
import HorizontalLine from "@farjs/ui/border/HorizontalLine.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import withStacksContext from "@farjs/filelist/stack/withStacksContext.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import MockFileListActions from "@farjs/filelist/MockFileListActions.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import withStackContext from "@farjs/filelist/stack/withStackContext.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import withThemeContext from "@farjs/filelist/theme/withThemeContext.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import QuickViewDir from "../../../viewer/quickview/QuickViewDir.mjs";
import QuickViewFile from "../../../viewer/quickview/QuickViewFile.mjs";
import QuickViewPanel from "../../../viewer/quickview/QuickViewPanel.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

QuickViewPanel.doubleBorderComp = mockComponent(DoubleBorder);
QuickViewPanel.horizontalLineComp = mockComponent(HorizontalLine);
QuickViewPanel.textLineComp = mockComponent(TextLine);
QuickViewPanel.quickViewDirComp = mockComponent(QuickViewDir);
QuickViewPanel.quickViewFileComp = mockComponent(QuickViewFile);

const {
  doubleBorderComp,
  horizontalLineComp,
  textLineComp,
  quickViewDirComp,
  quickViewFileComp,
} = QuickViewPanel;

const currComp = () => h("test stub");
const fsComp = () => h("test stub");

const [width, height] = [25, 15];

describe("QuickViewPanel.test.mjs", () => {
  it("should render dir view", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const state = {
      ...FileListState(),
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [
          FileListItem.up,
          { ...FileListItem("file"), size: 2 },
          { ...FileListItem("dir", true), size: 1 },
        ],
      },
    };
    /** @type {QuickViewParams} */
    const params = {
      name: "",
      parent: "",
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {WithStackProps} */
    const leftPanelStack = {
      isRight: false,
      panelInput: /** @type {BlessedElement} */ ({}),
      stack: new PanelStack(
        false,
        stackState,
        (f) => (stackState = f(stackState))
      ),
      width,
      height,
    };
    /** @type {WithStackProps} */
    const rightPanelStack = {
      isRight: true,
      panelInput: /** @type {BlessedElement} */ ({}),
      stack: new PanelStack(
        true,
        [new PanelStackItem(fsComp, dispatch, actions, state)],
        mockFunction()
      ),
      width,
      height,
    };
    const stack = leftPanelStack;

    //when
    const result = TestRenderer.create(
      withStacksContext(
        withStackContext(withThemeContext(h(QuickViewPanel, null)), stack),
        WithStacksProps(
          WithStacksData(leftPanelStack.stack, leftPanelStack.panelInput),
          WithStacksData(rightPanelStack.stack, rightPanelStack.panelInput)
        )
      )
    ).root;

    //then
    const dir = FileListItem.currDir;
    assertQuickViewPanel(result, dispatch, actions, state, stack, dir);
  });

  it("should render file view", () => {
    //given
    const dispatch = mockFunction();
    const actions = new MockFileListActions();
    const file = { ...FileListItem("file"), size: 2 };
    const state = {
      ...FileListState(),
      index: 1,
      currDir: {
        path: "/sub-dir",
        isRoot: false,
        items: [
          FileListItem.up,
          file,
          { ...FileListItem("dir", true), size: 1 },
        ],
      },
    };
    /** @type {QuickViewParams} */
    const params = {
      name: "",
      parent: "",
      folders: 0,
      files: 0,
      filesSize: 0,
    };
    /** @type {readonly PanelStackItem<any>[]} */
    let stackState = [
      new PanelStackItem(currComp, undefined, undefined, params),
    ];
    /** @type {WithStackProps} */
    const leftPanelStack = {
      isRight: false,
      panelInput: /** @type {BlessedElement} */ ({}),
      stack: new PanelStack(
        true,
        [new PanelStackItem(fsComp, dispatch, actions, state)],
        mockFunction()
      ),
      width,
      height,
    };
    /** @type {WithStackProps} */
    const rightPanelStack = {
      isRight: true,
      panelInput: /** @type {BlessedElement} */ ({}),
      stack: new PanelStack(
        false,
        stackState,
        (f) => (stackState = f(stackState))
      ),
      width,
      height,
    };
    const stack = rightPanelStack;

    //when
    const result = TestRenderer.create(
      withStacksContext(
        withStackContext(withThemeContext(h(QuickViewPanel, null)), stack),
        WithStacksProps(
          WithStacksData(leftPanelStack.stack, leftPanelStack.panelInput),
          WithStacksData(rightPanelStack.stack, rightPanelStack.panelInput)
        )
      )
    ).root;

    //then
    assertQuickViewPanel(result, dispatch, actions, state, stack, file);
  });
});

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {Dispatch} dispatch
 * @param {FileListActions} actions
 * @param {FileListState} state
 * @param {WithStackProps} panelStack
 * @param {FileListItem} currItem
 */
function assertQuickViewPanel(
  result,
  dispatch,
  actions,
  state,
  panelStack,
  currItem
) {
  assert.deepEqual(QuickViewPanel.displayName, "QuickViewPanel");

  const theme = FileListTheme.defaultTheme.fileList;
  const inputRef = (() => {
    /** @type {React.MutableRefObject<BlessedElement | null>} */
    const inputRef = currItem.isDir
      ? React.createRef()
      : result.findByType(quickViewFileComp).props.inputRef;

    if (!currItem.isDir) {
      assert.deepEqual(inputRef.current === panelStack.panelInput, true);
    }

    return inputRef;
  })();

  assertComponents(
    result.children,
    h(
      "box",
      { style: theme.regularItem },
      h(doubleBorderComp, {
        width,
        height,
        style: theme.regularItem,
      }),
      h(horizontalLineComp, {
        left: 0,
        top: height - 4,
        length: width,
        lineCh: SingleChars.horizontal,
        style: theme.regularItem,
        startCh: DoubleChars.leftSingle,
        endCh: DoubleChars.rightSingle,
      }),
      h(textLineComp, {
        align: TextAlign.center,
        left: 1,
        top: 0,
        width: width - 2,
        text: "Quick view",
        style: theme.regularItem,
        focused: panelStack.stack.isActive,
      }),

      currItem.isDir
        ? h(quickViewDirComp, {
            dispatch,
            actions,
            state,
            stack: panelStack.stack,
            width,
            currItem,
          })
        : h(
            "box",
            {
              left: 1,
              top: 1,
              width: width - 2,
              height: height - 5,
              style: theme.regularItem,
            },
            h(quickViewFileComp, {
              dispatch,
              inputRef,
              isRight: panelStack.isRight,
              filePath: path.join(state.currDir.path, currItem.name),
              size: currItem.size,
            })
          ),

      h("text", {
        width: width - 2,
        height: 2,
        left: 1,
        top: height - 3,
        style: theme.regularItem,
        content: currItem.name,
      })
    )
  );
}
