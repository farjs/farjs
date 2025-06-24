/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs";
 * @import { ViewerFileViewport } from "./ViewerFileViewport.mjs";
 */
import React, { useRef, useState } from "react";
import Popup from "@farjs/ui/popup/Popup.mjs";
import BottomMenu from "@farjs/ui/menu/BottomMenu.mjs";
import ViewerHeader from "./ViewerHeader.mjs";
import ViewerController from "./ViewerController.mjs";

const h = React.createElement;

/**
 * @param {string} filePath
 * @param {number} size
 */
function ViewerPluginUi(filePath, size) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const ViewerPluginUiComp = (props) => {
    const {
      popupComp,
      viewerHeader,
      viewerController,
      bottomMenuComp,
      defaultMenuItems,
    } = ViewerPluginUi;

    const inputRef = /** @type {React.MutableRefObject<BlessedElement>} */ (
      useRef()
    );
    const [viewport, setViewport] = useState(
      /** @type {ViewerFileViewport | undefined} */ (undefined)
    );

    /** @type {(keyFull: string) => boolean} */
    function onKeypress(keyFull) {
      var processed = true;
      switch (keyFull) {
        case "f3":
        case "f10":
          props.onClose();
          break;
        default:
          processed = false;
          break;
      }
      return processed;
    }

    const menuItems = (() => {
      if (!viewport) {
        return defaultMenuItems;
      }

      const items = [...defaultMenuItems];
      if (viewport.wrap) {
        items[1] = "Unwrap";
      }
      return items;
    })();

    return h(
      popupComp,
      { onClose: props.onClose, onKeypress },
      h(
        "box",
        {
          clickable: true,
          autoFocus: false,
        },
        h(viewerHeader, {
          filePath: filePath,
          encoding: viewport?.encoding ?? "",
          size: viewport?.size ?? 0,
          column: viewport?.column ?? 0,
          percent: viewport?.progress ?? 0,
        }),

        h(
          "button",
          {
            ref: (/** @type {BlessedElement} */ el) => {
              inputRef.current = el;
            },
            top: 1,
            width: "100%",
            height: "100%-2",
          },
          h(viewerController, {
            inputRef,
            dispatch: props.dispatch,
            filePath,
            size,
            viewport,
            setViewport,
            onKeypress: () => false,
          })
        ),

        h("box", { top: "100%-1" }, h(bottomMenuComp, { items: menuItems }))
      )
    );
  };

  ViewerPluginUiComp.displayName = "ViewerPluginUi";

  return ViewerPluginUiComp;
}

ViewerPluginUi.popupComp = Popup;
ViewerPluginUi.viewerHeader = ViewerHeader;
ViewerPluginUi.viewerController = ViewerController;
ViewerPluginUi.bottomMenuComp = BottomMenu;

/** @type {readonly string[]} */
ViewerPluginUi.defaultMenuItems = [
  /*  F1 */ "",
  /*  F2 */ "Wrap",
  /*  F3 */ "Quit",
  /*  F4 */ "",
  /*  F5 */ "",
  /*  F6 */ "",
  /*  F7 */ "",
  /*  F8 */ "Encodings",
  /*  F9 */ "",
  /* F10 */ "Quit",
  /* F11 */ "",
  /* F12 */ "DevTools",
];

export default ViewerPluginUi;
