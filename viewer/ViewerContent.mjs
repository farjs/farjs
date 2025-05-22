/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("@farjs/blessed").Widgets.Types.TStyle} BlessedStyle
 * @typedef {import("@farjs/filelist/theme/FileListTheme.mjs").FileListTheme} FileListTheme
 * @import { ViewerFileViewport } from "./ViewerFileViewport.mjs"
 */
import React, { useLayoutEffect, useRef, useState } from "react";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import EncodingsPopup from "../file/popups/EncodingsPopup.mjs";
import TextSearchPopup from "../file/popups/TextSearchPopup.mjs";
import ViewerInput from "./ViewerInput.mjs";
import ViewerSearch from "./ViewerSearch.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly inputRef: React.MutableRefObject<BlessedElement | null>;
 *  readonly viewport: ViewerFileViewport;
 *  setViewport(viewport: ViewerFileViewport | undefined): void;
 *  onKeypress(keyFull: string): boolean;
 * }} ViewerContentProps
 */

/**
 * @param {ViewerContentProps} props
 */
const ViewerContent = (props) => {
  const { viewerInput, encodingsPopup, textSearchPopup, viewerSearch } =
    ViewerContent;

  const theme = FileListTheme.useTheme();
  const viewport = props.viewport;
  const readP = useRef(Promise.resolve(viewport));
  const [showEncodingsPopup, setShowEncodingsPopup] = useState(false);
  const [showSearchPopup, setShowSearchPopup] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");

  /** @type {(viewport: ViewerFileViewport) => Promise<ViewerFileViewport>} */
  function updated(viewport) {
    props.setViewport(viewport);
    return Promise.resolve(viewport);
  }

  /** @type {(lines: number, from?: number) => void} */
  function onMoveUp(lines, from = viewport.position) {
    readP.current = readP.current
      .then((viewport) => viewport.moveUp(lines, from))
      .then(updated);
  }

  /** @type {(lines: number) => void} */
  function onMoveDown(lines) {
    readP.current = readP.current
      .then((viewport) => viewport.moveDown(lines))
      .then(updated);
  }

  /** @type {(from?: number) => void} */
  function onReload(from = viewport.position) {
    readP.current = readP.current
      .then((viewport) => viewport.reload(from))
      .then(updated);
  }

  /** @type {() => void} */
  function onWrap() {
    readP.current = readP.current.then((viewport) => {
      const wrap = !viewport.wrap;
      const column = wrap ? 0 : viewport.column;
      return updated(viewport.updated({ wrap, column }));
    });
  }

  /** @type {(encoding: string) => void} */
  function onEncoding(encoding) {
    readP.current = readP.current.then((viewport) =>
      updated(viewport.updated({ encoding }))
    );
  }

  /** @type {(dx: number) => void} */
  function onColumn(dx) {
    readP.current = readP.current.then((viewport) => {
      const col = viewport.column + dx;
      if (col >= 0 && col < 1000) {
        return updated(viewport.updated({ column: col }));
      }

      return viewport;
    });
  }

  /** @type {(keyFull: string) => void} */
  function onKeypress(keyFull) {
    if (!props.onKeypress(keyFull)) {
      switch (keyFull) {
        case "f2":
          onWrap();
          break;
        case "f7":
          setShowSearchPopup(true);
          break;
        case "f8":
          setShowEncodingsPopup(true);
          break;
        case "left":
          onColumn(-1);
          break;
        case "right":
          onColumn(1);
          break;
        case "C-r":
          onReload();
          break;
        case "home":
          onReload(0);
          break;
        case "end":
          onMoveUp(viewport.height, viewport.size);
          break;
        case "up":
          onMoveUp(1);
          break;
        case "pageup":
          onMoveUp(viewport.height);
          break;
        case "down":
          onMoveDown(1);
          break;
        case "pagedown":
          onMoveDown(viewport.height);
          break;
      }
    }
  }

  useLayoutEffect(() => {
    readP.current = readP.current.then(() => viewport.reload()).then(updated);
  }, [
    viewport.encoding,
    viewport.size,
    viewport.width,
    viewport.height,
    viewport.wrap,
  ]);

  return h(
    viewerInput,
    {
      inputRef: props.inputRef,
      onWheel: (up) => {
        if (up) onMoveUp(1);
        else onMoveDown(1);
      },
      onKeypress,
    },
    h("text", {
      width: viewport.width,
      height: viewport.height,
      style: ViewerContent.contentStyle(theme),
      wrap: false,
      content: viewport.content,
    }),

    showEncodingsPopup
      ? h(encodingsPopup, {
          encoding: props.viewport.encoding,
          onApply: onEncoding,
          onClose: () => {
            setShowEncodingsPopup(false);
          },
        })
      : null,

    showSearchPopup
      ? h(textSearchPopup, {
          onSearch: (term) => {
            setShowSearchPopup(false);
            setSearchTerm(term);
          },
          onCancel: () => {
            setShowSearchPopup(false);
          },
        })
      : null,

    searchTerm.length > 0
      ? h(viewerSearch, {
          searchTerm,
          onComplete: () => {
            setSearchTerm("");
          },
        })
      : null
  );
};

ViewerContent.displayName = "ViewerContent";
ViewerContent.viewerInput = ViewerInput;
ViewerContent.encodingsPopup = EncodingsPopup;
ViewerContent.textSearchPopup = TextSearchPopup;
ViewerContent.viewerSearch = ViewerSearch;

/** @type {(theme: FileListTheme) => BlessedStyle} */
ViewerContent.contentStyle = (theme) => {
  const style = theme.fileList.regularItem;
  return {
    bold: style.bold,
    bg: style.bg,
    fg: style.fg,
  };
};

export default ViewerContent;
