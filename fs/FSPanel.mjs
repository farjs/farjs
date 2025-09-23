/**
 * @typedef {import("@farjs/blessed").Widgets.Screen} BlessedScreen
 * @import { FileListPanelProps } from "@farjs/filelist/FileListPanel.mjs"
 * @typedef {import("./FSService.mjs").FSService} FSService
 */
import React from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPanel from "@farjs/filelist/FileListPanel.mjs";
import FSFreeSpace from "./FSFreeSpace.mjs";
import FSFoldersHistory from "./FSFoldersHistory.mjs";
import FSService from "./FSService.mjs";

const h = React.createElement;

/**
 * @param {FileListPanelProps} props
 */
const FSPanel = (props) => {
  const { fsService, fileListPanelComp, fsFreeSpaceComp, fsFoldersHistory } =
    FSPanel;

  /** @type {(screen: BlessedScreen, key: string) => boolean} */
  function onKeypress(_, key) {
    let processed = true;
    switch (key) {
      case "M-o":
        const item = FileListState.currentItem(props.state);
        if (item) {
          const parent = props.state.currDir.path;
          const p = fsService.openItem(parent, item.name);
          props.dispatch(TaskAction(Task("Opening default app", p)));
        }
        break;
      default:
        processed = false;
        break;
    }

    return processed;
  }

  return h(
    React.Fragment,
    null,

    h(fileListPanelComp, { ...props, onKeypress }),

    h(fsFreeSpaceComp, {
      dispatch: props.dispatch,
      currDir: props.state.currDir,
    }),

    h(fsFoldersHistory, {
      currDirPath: props.state.currDir.path,
    })
  );
};

FSPanel.displayName = "FSPanel";
/** @type {FSService} */
FSPanel.fsService = FSService.instance;
FSPanel.fileListPanelComp = FileListPanel;
FSPanel.fsFreeSpaceComp = FSFreeSpace;
FSPanel.fsFoldersHistory = FSFoldersHistory;

export default FSPanel;
