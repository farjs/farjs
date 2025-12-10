/**
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import { FileListState } from "@farjs/filelist/FileListState.mjs"
 * @import { FileListParamsChangedAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { AddToArchAction } from "./AddToArchPopup.mjs"
 */
import React, { useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import AddToArchPopup from "./AddToArchPopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch
 *  readonly actions: FileListActions
 *  readonly state: FileListState
 *  readonly archName: String
 *  readonly archType: String
 *  readonly archAction: AddToArchAction
 *  addToArchApi(archFile: string, parent: string, items: Set<string>, onProgress: () => void): Promise<void>
 *  readonly items: FileListItem[]
 *  onComplete(archFile: string): void;
 *  onCancel(): void;
 * }} AddToArchControllerProps
 */

/**
 * @param {AddToArchControllerProps} props
 */
const AddToArchController = (props) => {
  const { addToArchPopup, statusPopupComp } = AddToArchController;

  const [showAddPopup, setShowAddPopup] = useState(true);
  const [showStatusPopup, setShowStatusPopup] = useState(false);
  const [progress, setProgress] = useState(0);

  /** @type {(archFile: string) => void} */
  const onAction = (archFile) => {
    setShowAddPopup(false);
    setShowStatusPopup(true);

    const parent = props.state.currDir.path;
    const currItems = props.items;
    let totalItems = currItems.length;
    let addedItems = 0;

    const resultP = (async () => {
      await props.actions.scanDirs(parent, currItems, (_, items) => {
        totalItems += items.length;
        return true;
      });
      await props.addToArchApi(
        archFile,
        parent,
        new Set(currItems.map((_) => _.name)),
        () => {
          addedItems += 1;
          setProgress(
            Math.trunc(Math.min((addedItems * 100) / totalItems, 100))
          );
        }
      );

      if (props.state.selectedNames.size > 0) {
        /** @type {FileListParamsChangedAction} */
        const action = {
          action: "FileListParamsChangedAction",
          offset: props.state.offset,
          index: props.state.index,
          selectedNames: new Set(),
        };
        props.dispatch(action);
      }

      setShowStatusPopup(false);
      props.onComplete(archFile);
    })();

    resultP.catch(() => {
      setShowStatusPopup(false);
      props.dispatch(
        TaskAction(
          Task(
            `${props.archAction} item(s) to ${props.archType} archive`,
            resultP
          )
        )
      );
    });
  };

  return h(
    React.Fragment,
    null,
    showAddPopup
      ? h(addToArchPopup, {
          archName: props.archName,
          archType: props.archType,
          action: props.archAction,
          onAction,
          onCancel: props.onCancel,
        })
      : null,

    showStatusPopup
      ? h(statusPopupComp, {
          text: `${props.archAction} item(s) to ${props.archType} archive\n${progress}%`,
        })
      : null
  );
};

AddToArchController.displayName = "AddToArchController";
AddToArchController.addToArchPopup = AddToArchPopup;
AddToArchController.statusPopupComp = StatusPopup;

export default AddToArchController;
