/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("@farjs/blessed").Widgets.Types.TStyle} BlessedStyle
 * @typedef {import("@farjs/filelist/theme/FileListTheme.mjs").FileListTheme} FileListTheme
 * @typedef {import("../file/FileViewHistory.mjs").FileViewHistory} FileViewHistory
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { ViewerFileViewport } from "./ViewerFileViewport.mjs"
 */
import React, { useLayoutEffect, useRef } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import WithSize from "@farjs/ui/WithSize.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import Encoding from "../file/Encoding.mjs";
import FileReader from "../file/FileReader.mjs";
import FileViewHistory from "../file/FileViewHistory.mjs";
import ViewerFileReader from "./ViewerFileReader.mjs";
import { createViewerFileViewport } from "./ViewerFileViewport.mjs";
import ViewerContent from "./ViewerContent.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly inputRef: React.MutableRefObject<BlessedElement | null>;
 *  readonly dispatch: Dispatch;
 *  readonly filePath: string;
 *  readonly size: number;
 *  readonly viewport?: ViewerFileViewport;
 *  setViewport(viewport: ViewerFileViewport | undefined): void;
 *  onKeypress(keyFull: string): boolean;
 * }} ViewerControllerProps
 */

/**
 * @param {ViewerControllerProps} props
 */
const ViewerController = (props) => {
  const { withSizeComp, viewerContent } = ViewerController;

  const theme = FileListTheme.useTheme();
  const historyProvider = HistoryProvider.useHistoryProvider();
  const viewportRef = useRef(props.viewport);
  viewportRef.current = props.viewport;

  useLayoutEffect(() => {
    /** @type {(viewerFileReader: ViewerFileReader) => Promise<void>} */
    async function open(viewerFileReader) {
      const fileViewsHistory = await historyProvider.get(
        FileViewHistory.fileViewsHistoryKind
      );
      const historyItem = FileViewHistory.pathToItem(props.filePath, false);
      const maybeHistory = await fileViewsHistory.getOne(historyItem);
      await viewerFileReader.open(props.filePath);

      const history = maybeHistory
        ? FileViewHistory.fromHistory(maybeHistory)
        : undefined;
      props.setViewport(
        createViewerFileViewport(
          viewerFileReader,
          history ? history.params.encoding : Encoding.platformEncoding,
          props.size,
          0,
          0,
          history ? history.params.wrap : false,
          history ? history.params.column : 0,
          history ? history.params.position : 0
        )
      );
    }

    /** @type {(vp: ViewerFileViewport) => Promise<void>} */
    async function close(vp) {
      const history = FileViewHistory(props.filePath, {
        isEdit: false,
        encoding: vp.encoding,
        position: vp.position,
        wrap: vp.wrap,
        column: vp.column,
      });
      const fileViewsHistory = await historyProvider.get(
        FileViewHistory.fileViewsHistoryKind
      );
      await fileViewsHistory.save(FileViewHistory.toHistory(history));
    }

    const viewerFileReader = ViewerController._createFileReader();
    const openP = open(viewerFileReader);
    openP.catch(() => {
      props.dispatch(TaskAction(Task("Opening file", openP)));
    });

    return () => {
      viewerFileReader.close();
      const vp = viewportRef.current;
      if (vp) {
        close(vp);
      }
    };
  }, []);

  const viewport = props.viewport;
  return h(withSizeComp, {
    render: (width, height) => {
      return h(
        "box",
        { style: ViewerContent.contentStyle(theme) },
        ...(() => {
          if (viewport) {
            const linesCount = viewport.linesData.length;

            return [
              h(
                React.Fragment,
                null,
                ...[
                  h(viewerContent, {
                    inputRef: props.inputRef,
                    viewport: viewport.updated({
                      width,
                      height,
                    }),
                    setViewport: props.setViewport,
                    onKeypress: props.onKeypress,
                  }),

                  viewport.column > 0 && linesCount > 0
                    ? h("text", {
                        key: "leftScrollIndicators",
                        style: ViewerController.scrollStyle(theme),
                        width: 1,
                        height: linesCount,
                        content: "<".repeat(linesCount),
                      })
                    : null,

                  ...viewport.scrollIndicators.map((lineIdx) =>
                    h("text", {
                      key: `${lineIdx}`,
                      style: ViewerController.scrollStyle(theme),
                      left: width - 1,
                      top: lineIdx,
                      width: 1,
                      height: 1,
                      content: ">",
                    })
                  ),
                ]
              ),
            ];
          }

          return [];
        })()
      );
    },
  });
};

ViewerController.displayName = "ViewerController";
ViewerController.withSizeComp = WithSize;
ViewerController.viewerContent = ViewerContent;

/** @type {() => ViewerFileReader} */
ViewerController._createFileReader = () =>
  new ViewerFileReader(new FileReader());

/** @type {(theme: FileListTheme) => BlessedStyle} */
ViewerController.scrollStyle = (theme) => {
  const style = theme.fileList.header;
  return {
    bold: style.bold,
    bg: style.bg,
    fg: style.fg,
  };
};

export default ViewerController;
