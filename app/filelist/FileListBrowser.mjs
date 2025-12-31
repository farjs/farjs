/**
 * @typedef {import("@farjs/blessed").Widgets.Events.IKeyEventArg & {
 *    data?: any
 * }} IKeyEventArg
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { Dispatch, ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPluginHandler } from "./FileListPluginHandler.mjs"
 */
import React, { useLayoutEffect, useRef, useState } from "react";
import BottomMenu from "@farjs/ui/menu/BottomMenu.mjs";
import MenuBarTrigger from "@farjs/ui/menu/MenuBarTrigger.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStack from "@farjs/filelist/stack/WithStack.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import WithStacksData from "@farjs/filelist/stack/WithStacksData.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FSPlugin from "../../fs/FSPlugin.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly isRightInitiallyActive: boolean;
 * }} FileListBrowserProps
 */

/**
 * @param {FileListPluginHandler} pluginHandler
 */
function FileListBrowser(pluginHandler) {
  /**
   * @param {FileListBrowserProps} props
   */
  const FileListBrowserComp = (props) => {
    const { withStackComp, bottomMenuComp, menuBarTrigger, fsPlugin } =
      FileListBrowser;

    const leftButtonRef =
      /** @type {React.MutableRefObject<BlessedElement>} */ (useRef());
    const rightButtonRef =
      /** @type {React.MutableRefObject<BlessedElement>} */ (useRef());
    const [isRight, setIsRight] = useState(false);
    const [isRightActive, setIsRightActive] = useState(
      props.isRightInitiallyActive
    );
    /** @type {[ReactComponent | undefined, React.Dispatch<React.SetStateAction<ReactComponent | undefined>>]} */
    const [currPluginUi, setCurrPluginUi] = useState();
    const [leftStackData, setLeftStackData] = useState(
      () =>
        /** @type {readonly PanelStackItem<any>[]} */ ([
          new PanelStackItem(fsPlugin.component),
        ])
    );
    const [rightStackData, setRightStackData] = useState(
      () =>
        /** @type {readonly PanelStackItem<any>[]} */ ([
          new PanelStackItem(fsPlugin.component),
        ])
    );
    const leftStack = new PanelStack(
      !isRightActive,
      leftStackData,
      setLeftStackData
    );
    const rightStack = new PanelStack(
      isRightActive,
      rightStackData,
      setRightStackData
    );

    /** @type {(isRightActive: boolean) => BlessedElement} */
    function getInput(isRightActive) {
      const [leftEl, rightEl] = isRight
        ? [rightButtonRef.current, leftButtonRef.current]
        : [leftButtonRef.current, rightButtonRef.current];

      return isRightActive ? rightEl : leftEl;
    }

    /** @type {(isRight: boolean) => PanelStack} */
    function getStack(isRight) {
      return isRight ? rightStack : leftStack;
    }

    /** @type {(isRight: boolean) => () => void} */
    function onActivate(isRight) {
      return () => {
        const stack = getStack(isRight);
        if (!stack.isActive) {
          setIsRightActive(isRight);
        }
      };
    }

    const stacks = WithStacksProps(
      WithStacksData(getStack(isRight), leftButtonRef.current),
      WithStacksData(getStack(!isRight), rightButtonRef.current)
    );

    /** @type {(obj: object, key: IKeyEventArg) => void} */
    function onKeypress(_, key) {
      const screen = () => leftButtonRef.current.screen;
      switch (key.full) {
        case "tab":
        case "S-tab":
          screen().focusNext();
          break;
        case "C-u":
          setIsRight((_) => !_);
          screen().focusNext();
          break;
        case "enter":
        case "C-pagedown":
          pluginHandler.openCurrItem(props.dispatch, getStack(isRightActive));
          break;
        default:
          pluginHandler
            .openPluginUi(props.dispatch, key, stacks)
            .then((pluginUi) => {
              if (pluginUi) {
                setCurrPluginUi(() => pluginUi);
              }
            });
          break;
      }
    }

    useLayoutEffect(() => {
      fsPlugin.init(props.dispatch, leftStack);
      fsPlugin.init(props.dispatch, rightStack);

      getInput(isRightActive).focus();
    }, []);

    return h(
      WithStacks,
      stacks,
      h(
        "button",
        {
          isRight: false,
          ref: leftButtonRef,
          mouse: true,
          width: "50%",
          height: "100%-1",
          onFocus: onActivate(isRight),
          onKeypress,
        },
        h(withStackComp, {
          isRight: false,
          panelInput: leftButtonRef.current,
          stack: getStack(isRight),
          width: 0,
          height: 0,
        })
      ),
      h(
        "button",
        {
          isRight: true,
          ref: rightButtonRef,
          mouse: true,
          width: "50%",
          height: "100%-1",
          left: "50%",
          onFocus: onActivate(!isRight),
          onKeypress,
        },
        h(withStackComp, {
          isRight: true,
          panelInput: rightButtonRef.current,
          stack: getStack(!isRight),
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
      h(menuBarTrigger),

      currPluginUi
        ? h(currPluginUi, {
            dispatch: props.dispatch,
            onClose: () => {
              setCurrPluginUi(undefined);
            },
          })
        : null
    );
  };

  FileListBrowserComp.displayName = "FileListBrowser";

  return FileListBrowserComp;
}

FileListBrowser.withStackComp = WithStack;
FileListBrowser.bottomMenuComp = BottomMenu;
FileListBrowser.menuBarTrigger = MenuBarTrigger;
FileListBrowser.fsPlugin = FSPlugin.instance;

/** @type {readonly string[]} */
FileListBrowser.menuItems = Object.freeze([
  /*  F1 */ "",
  /*  F2 */ "",
  /*  F3 */ "View",
  /*  F4 */ "",
  /*  F5 */ "Copy",
  /*  F6 */ "RenMov",
  /*  F7 */ "MkFolder",
  /*  F8 */ "Delete",
  /*  F9 */ "Menu",
  /* F10 */ "Exit",
  /* F11 */ "",
  /* F12 */ "DevTools",
]);

export default FileListBrowser;
