/**
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 */
import React, { useLayoutEffect, useRef, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TextAlign from "@farjs/ui/TextAlign.mjs";
import TextLine from "@farjs/ui/TextLine.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import { formatSize } from "@farjs/filelist/utils.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly name: string;
 *  readonly parent: string;
 *  readonly folders: number;
 *  readonly files: number;
 *  readonly filesSize: number;
 * }} QuickViewParams
 */

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly actions: FileListActions;
 *  readonly state: FileListState;
 *  readonly stack: PanelStack;
 *  readonly width: number;
 *  readonly currItem: FileListItem;
 * }} QuickViewDirProps
 */

/**
 * @param {QuickViewDirProps} props
 */
const QuickViewDir = (props) => {
  const { statusPopupComp, textLineComp } = QuickViewDir;

  const [showPopup, setShowPopup] = useState(false);
  const inProgress = useRef(false);

  const stack = props.stack;
  /** @type {QuickViewParams} */
  const params = stack.params();
  const theme = FileListTheme.useTheme().fileList;

  /**
   * @returns {void}
   */
  function scanDir() {
    const parent = props.state.currDir.path;

    let folders = 0;
    let files = 0;
    let filesSize = 0;
    /** @type {QuickViewParams} */
    const params = {
      name: props.currItem.name,
      parent,
      folders,
      files,
      filesSize,
    };
    stack.update((_) => _.withState(params));

    const resultP = props.actions.scanDirs(
      parent,
      [props.currItem],
      (_, items) => {
        items.forEach((i) => {
          if (i.isDir) folders += 1;
          else {
            files += 1;
            filesSize += i.size;
          }
        });

        return inProgress.current;
      }
    );

    resultP.then(
      (res) => {
        if (res) {
          setShowPopup(false);
          const newParams = { ...params, folders, files, filesSize };
          stack.update((_) => _.withState(newParams));
        }
      },
      () => {
        setShowPopup(false);
        props.dispatch(TaskAction(Task("Quick view dir scan", resultP)));
      }
    );
  }

  useLayoutEffect(() => {
    if (!inProgress.current && showPopup) {
      // start scan
      inProgress.current = true;
      scanDir();
    } else if (inProgress.current && !showPopup) {
      // stop scan
      inProgress.current = false;
    }
  }, [showPopup]);

  useLayoutEffect(() => {
    if (
      params.name !== props.currItem.name ||
      params.parent !== props.state.currDir.path
    ) {
      setShowPopup(true);
    }
  }, [props.currItem.name, props.state.currDir.path, params]);

  return h(
    React.Fragment,
    null,
    showPopup
      ? h(statusPopupComp, {
          text: `Scanning the folder\n${props.currItem.name}`,
          title: "View Dir",
          onClose: () => {
            setShowPopup(false);
          },
        })
      : null,

    h("text", {
      left: 2,
      top: 2,
      style: theme.regularItem,
      content: `Folder

Contains:

Folders
Files
Files size`,
    }),

    h(textLineComp, {
      align: TextAlign.left,
      left: 12,
      top: 2,
      width: props.width - 14,
      text: `"${props.currItem.name}"`,
      style: theme.regularItem,
      padding: 0,
    }),

    h("text", {
      left: 15,
      top: 6,
      style: theme.selectedItem,
      content: `${formatSize(params.folders)}
${formatSize(params.files)}
${formatSize(params.filesSize)}`,
    })
  );
};

QuickViewDir.displayName = "QuickViewDir";
QuickViewDir.statusPopupComp = StatusPopup;
QuickViewDir.textLineComp = TextLine;

export default QuickViewDir;
