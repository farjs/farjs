/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { ViewerFileViewport } from "../ViewerFileViewport.mjs";
 */
import React, { useState } from "react";
import ViewerController from "../ViewerController.mjs";
import ViewerEvent from "../ViewerEvent.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly inputRef: React.MutableRefObject<BlessedElement | null>;
 *  readonly isRight: boolean;
 *  readonly filePath: string;
 *  readonly size: number;
 * }} QuickViewFileProps
 */

/**
 * @param {QuickViewFileProps} props
 */
const QuickViewFile = (props) => {
  const { viewerController } = QuickViewFile;

  const [viewport, setViewport] = useState(
    /** @type {ViewerFileViewport | undefined} */ (undefined)
  );

  /** @type {(key: string) => boolean} */
  const onKeypress = (key) => {
    let processed = true;
    const inputEl = props.inputRef.current;

    switch (key) {
      case "f3":
        inputEl?.emit("keypress", undefined, {
          name: "",
          full: props.isRight
            ? ViewerEvent.onViewerOpenLeft
            : ViewerEvent.onViewerOpenRight,
        });
        break;
      default:
        processed = false;
        break;
    }

    return processed;
  };

  return h(viewerController, {
    inputRef: props.inputRef,
    dispatch: props.dispatch,
    filePath: props.filePath,
    size: props.size,
    viewport,
    setViewport,
    onKeypress,
  });
};

QuickViewFile.displayName = "QuickViewFile";
QuickViewFile.viewerController = ViewerController;

export default QuickViewFile;
