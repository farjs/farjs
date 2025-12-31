/**
 * @typedef {import("@farjs/blessed").Widgets.Screen} BlessedScreen
 * @typedef {import("@farjs/filelist/stack/WithStacksProps.mjs").WithStacksProps} WithStacksProps
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListBrowserProps } from "../../../app/filelist/FileListBrowser.mjs"
 */
import React from "react";
import TestRenderer from "react-test-renderer";
import assert from "node:assert/strict";
import { actAsync, assertComponents, mockComponent } from "react-assert";
import mockFunction from "mock-fn";
import BottomMenu from "@farjs/ui/menu/BottomMenu.mjs";
import MenuBarTrigger from "@farjs/ui/menu/MenuBarTrigger.mjs";
import WithStack from "@farjs/filelist/stack/WithStack.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FSPlugin from "../../../fs/FSPlugin.mjs";
import FileListPluginHandler from "../../../app/filelist/FileListPluginHandler.mjs";
import FileListBrowser from "../../../app/filelist/FileListBrowser.mjs";

const h = React.createElement;

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

FileListBrowser.withStackComp = mockComponent(WithStack);
FileListBrowser.bottomMenuComp = mockComponent(BottomMenu);
FileListBrowser.menuBarTrigger = mockComponent(MenuBarTrigger);
FileListBrowser.fsPlugin = new FSPlugin((s, _) => s);

const fileListBrowser = FileListBrowser(FileListPluginHandler([]));
const { withStackComp, bottomMenuComp, menuBarTrigger } = FileListBrowser;

describe("FileListBrowser.test.mjs", () => {
  it("should not activate left stack if already active when onFocus in left panel", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({ dispatch });
    const focusMock = mockFunction();
    const leftButtonMock = { focus: focusMock };
    const rightButtonMock = {};
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        const isRight = el.props.isRight ?? false;
        return isRight && el.type === "button"
          ? rightButtonMock
          : el.type === "button"
          ? leftButtonMock
          : null;
      },
    }).root;
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, true);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, false);
    })();
    const leftButton = comp.findAllByType("button")[0];

    //when
    TestRenderer.act(() => {
      leftButton.props.onFocus();
    });

    //then
    assert.deepEqual(focusMock.times, 1);
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, true);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, false);
    })();
  });

  it("should activate left stack when onFocus in left panel", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({
      dispatch,
      isRightInitiallyActive: true,
    });
    const focusMock = mockFunction();
    const leftButtonMock = {};
    const rightButtonMock = { focus: focusMock };
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        const isRight = el.props.isRight ?? false;
        return isRight && el.type === "button"
          ? rightButtonMock
          : el.type === "button"
          ? leftButtonMock
          : null;
      },
    }).root;
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, false);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, true);
    })();
    const leftButton = comp.findAllByType("button")[0];

    //when
    TestRenderer.act(() => {
      leftButton.props.onFocus();
    });

    //then
    assert.deepEqual(focusMock.times, 1);
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, true);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, false);
    })();
  });

  it("should activate right stack when onFocus in right panel", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({ dispatch });
    const focusMock = mockFunction();
    const leftButtonMock = { focus: focusMock };
    const rightButtonMock = {};
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        const isRight = el.props.isRight ?? false;
        return isRight && el.type === "button"
          ? rightButtonMock
          : el.type === "button"
          ? leftButtonMock
          : null;
      },
    }).root;
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, true);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, false);
    })();
    const rightButton = comp.findAllByType("button")[1];

    //when
    TestRenderer.act(() => {
      rightButton.props.onFocus();
    });

    //then
    assert.deepEqual(focusMock.times, 1);
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, false);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, true);
    })();
  });

  it("should focus next panel when onKeypress(tab|S-tab)", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({ dispatch });
    const focusNextMock = mockFunction();
    /** @type {BlessedScreen} */
    const screen = /** @type {any} */ ({ focusNext: focusNextMock });
    const focusMock = mockFunction();
    const buttonMock = { screen, focus: focusMock };
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? buttonMock : null;
      },
    }).root;
    assert.deepEqual(focusMock.times, 1);
    const leftButton = comp.findAllByType("button")[0];

    /** @type {(keyFull: string, focus: boolean) => void} */
    function check(keyFull, focus) {
      //given
      const focusNextMockTimes = focusNextMock.times;

      //when
      TestRenderer.act(() => {
        leftButton.props.onKeypress(null, { full: keyFull });
      });

      //then
      assert.deepEqual(
        focusNextMock.times,
        focus ? focusNextMockTimes + 1 : focusNextMockTimes
      );
    }

    //when & then
    check("tab", true);
    check("S-tab", true);
    check("unknown", false);
  });

  it("should swap the panels when onKeypress(Ctrl+U)", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({ dispatch });
    const focusNextMock = mockFunction();
    /** @type {BlessedScreen} */
    const screen = /** @type {any} */ ({ focusNext: focusNextMock });
    const focusMock = mockFunction();
    const buttonMock = { screen, focus: focusMock };
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? buttonMock : null;
      },
    }).root;
    assert.deepEqual(focusMock.times, 1);
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, true);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, false);
    })();
    const leftButton = comp.findAllByType("button")[0];
    const keyFull = "C-u";

    //when
    TestRenderer.act(() => {
      leftButton.props.onKeypress(null, { full: keyFull });
    });

    //then
    assert.deepEqual(focusNextMock.times, 1);
    (() => {
      const [leftStack, rightStack] = comp
        .findAllByType(withStackComp)
        .map((_) => _.props);
      assert.deepEqual(leftStack.isRight, false);
      assert.deepEqual(leftStack.stack.isActive, false);
      assert.deepEqual(rightStack.isRight, true);
      assert.deepEqual(rightStack.stack.isActive, true);
    })();
  });

  it("should call pluginHandler.openCurrItem when onKeypress(enter)", () => {
    //given
    const dispatch = mockFunction();
    let openCurrItemArgs = /** @type {any[]} */ ([]);
    const openCurrItem = mockFunction((...args) => (openCurrItemArgs = args));
    const pluginHandler = {
      openCurrItem,
      openPluginUi: mockFunction(),
    };
    const fileListBrowser = FileListBrowser(pluginHandler);
    const props = getFileListBrowserProps({ dispatch });
    const focusMock = mockFunction();
    const buttonMock = { focus: focusMock };
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? buttonMock : null;
      },
    }).root;
    assert.deepEqual(focusMock.times, 1);
    const leftButton = comp.findAllByType("button")[0];
    const [leftStack, _] = comp
      .findAllByType(withStackComp)
      .map((_) => _.props);
    const keyFull = "enter";

    //when
    TestRenderer.act(() => {
      leftButton.props.onKeypress(null, { full: keyFull });
    });

    //then
    assert.deepEqual(openCurrItem.times, 1);
    assert.deepEqual(openCurrItemArgs, [props.dispatch, leftStack.stack]);
  });

  it("should call pluginHandler.openCurrItem when onKeypress(C-pagedown)", () => {
    //given
    const dispatch = mockFunction();
    let openCurrItemArgs = /** @type {any[]} */ ([]);
    const openCurrItem = mockFunction((...args) => (openCurrItemArgs = args));
    const pluginHandler = {
      openCurrItem,
      openPluginUi: mockFunction(),
    };
    const fileListBrowser = FileListBrowser(pluginHandler);
    const props = getFileListBrowserProps({ dispatch });
    const focusMock = mockFunction();
    const buttonMock = { focus: focusMock };
    const comp = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? buttonMock : null;
      },
    }).root;
    assert.deepEqual(focusMock.times, 1);
    const leftButton = comp.findAllByType("button")[0];
    const [leftStack, _] = comp
      .findAllByType(withStackComp)
      .map((_) => _.props);
    const keyFull = "C-pagedown";

    //when
    TestRenderer.act(() => {
      leftButton.props.onKeypress(null, { full: keyFull });
    });

    //then
    assert.deepEqual(openCurrItem.times, 1);
    assert.deepEqual(openCurrItemArgs, [props.dispatch, leftStack.stack]);
  });

  it("should call pluginHandler.openPluginUi and render plugin ui when onKeypress(triggerKey)", async () => {
    //given
    const dispatch = mockFunction();
    let openPluginUiArgs = /** @type {any[]} */ ([]);
    const openPluginUi = mockFunction((...args) => {
      openPluginUiArgs = args;
      return Promise.resolve(pluginUi);
    });
    const pluginHandler = {
      openCurrItem: mockFunction(),
      openPluginUi,
    };
    const fileListBrowser = FileListBrowser(pluginHandler);
    const props = getFileListBrowserProps({ dispatch });
    const focusMock = mockFunction();
    const buttonMock = { focus: focusMock };
    const renderer = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        return el.type === "button" ? buttonMock : null;
      },
    });
    assert.deepEqual(focusMock.times, 1);
    const leftButton = renderer.root.findAllByType("button")[0];
    const [leftStack, rightStack] = renderer.root
      .findAllByType(withStackComp)
      .map((_) => _.props);
    const stacks = WithStacksProps(
      WithStacksData(leftStack.stack, leftStack.panelInput),
      WithStacksData(rightStack.stack, rightStack.panelInput)
    );
    const [_, pluginUi] = getStacksCtxHook();
    const keyData = { full: "C-p" };

    //when
    await actAsync(() => {
      leftButton.props.onKeypress(null, keyData);
    });

    //then
    assert.deepEqual(openPluginUi.times, 1);
    assert.deepEqual(openPluginUiArgs, [props.dispatch, keyData, stacks]);
    const uiComp = renderer.root.findByType(pluginUi).props;
    assert.deepEqual(uiComp.dispatch === props.dispatch, true);

    //when
    TestRenderer.act(() => {
      uiComp.onClose();
    });

    //then
    assert.deepEqual(renderer.root.findAllByType(pluginUi).length, 0);
  });

  it("should render initial component and focus active panel", () => {
    //given
    const dispatch = mockFunction();
    const props = getFileListBrowserProps({
      dispatch,
      isRightInitiallyActive: true,
    });
    const leftButtonMock = {};
    const focusMock = mockFunction();
    const rightButtonMock = { focus: focusMock };

    //when
    const result = TestRenderer.create(h(fileListBrowser, props), {
      createNodeMock: (el) => {
        const isRight = el.props.isRight ?? false;
        return isRight && el.type === "button"
          ? rightButtonMock
          : el.type === "button"
          ? leftButtonMock
          : null;
      },
    }).root;

    //then
    assert.deepEqual(focusMock.times, 1);
    assertFileListBrowser(result, fileListBrowser);
  });
});

/**
 * @param {Partial<FileListBrowserProps>} props
 * @returns {FileListBrowserProps}
 */
function getFileListBrowserProps(props = {}) {
  return {
    dispatch: mockFunction(),
    isRightInitiallyActive: false,
    ...props,
  };
}

/**
 * @param {TestRenderer.ReactTestInstance} result
 * @param {ReactComponent} pluginUi
 */
function assertFileListBrowser(result, pluginUi) {
  assert.deepEqual(pluginUi.displayName, "FileListBrowser");

  const stacks = /** @type {WithStacksProps} */ (
    result.findByType(WithStacks).props
  );
  assert.deepEqual(stacks.left.stack.isActive, false);
  assert.deepEqual(stacks.right.stack.isActive, true);

  assertComponents(
    result.children,
    h(
      WithStacks,
      stacks,
      h(
        "button",
        {
          mouse: true,
          width: "50%",
          height: "100%-1",
        },
        h(withStackComp, {
          isRight: false,
          panelInput: stacks.left.input,
          stack: stacks.left.stack,
          width: 0,
          height: 0,
        })
      ),
      h(
        "button",
        {
          mouse: true,
          width: "50%",
          height: "100%-1",
          left: "50%",
        },
        h(withStackComp, {
          isRight: true,
          panelInput: stacks.right.input,
          stack: stacks.right.stack,
          width: 0,
          height: 0,
        })
      ),

      h(
        "box",
        {
          top: "100%-1",
        },
        h(bottomMenuComp, { items: FileListBrowser.menuItems })
      ),
      h(menuBarTrigger)
    )
  );
}

/**
 * @returns {[React.MutableRefObject<WithStacksProps | null>, () => React.ReactElement<any>]}
 */
function getStacksCtxHook() {
  /** @type {React.MutableRefObject<WithStacksProps | null>} */
  const ref = React.createRef();
  const comp = () => {
    const ctx = WithStacks.useStacks();
    ref.current = ctx;
    return h(React.Fragment, null);
  };

  return [ref, comp];
}
