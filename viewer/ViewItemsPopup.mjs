/**
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { FileListDirUpdatedAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { Dispatch, FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs"
 */
import React, { useLayoutEffect, useRef, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";

const h = React.createElement;

/**
 * @param {FileListData} data
 */
function ViewItemsPopup(data) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const ViewItemsPopupComp = (props) => {
    const { statusPopupComp } = ViewItemsPopup;

    const [currDir, setCurrDir] = useState("");
    const inProgress = useRef(true);

    /**
     * @param {Dispatch} dispatch
     * @param {FileListActions} actions
     * @param {FileListState} state
     * @returns {void}
     */
    function viewItems(dispatch, actions, state) {
      const parent = state.currDir.path;
      const currSelected = FileListState.selectedItems(state);
      const currItems =
        currSelected.length > 0
          ? currSelected
          : [FileListState.currentItem(state)].filter((_) => _ !== undefined);

      const sizes = currItems.reduce((res, i) => {
        return i.isDir ? res.set(i.name, 0) : res.set(i.name, i.size);
      }, /** @type {Map<string, number>} */ (new Map()));

      const resultP = currItems.reduce((resP, currItem) => {
        return resP.then((res) => {
          if (res && currItem.isDir) {
            setCurrDir(currItem.name);
            let currSize = 0;
            return actions
              .scanDirs(parent, [currItem], (_, items) => {
                currSize += items.reduce((res, i) => {
                  return res + (i.isDir ? 0 : i.size);
                }, 0);

                return inProgress.current;
              })
              .then((res) => {
                sizes.set(currItem.name, currSize);
                return res;
              });
          }

          return res;
        });
      }, Promise.resolve(true));

      resultP.then(
        (res) => {
          props.onClose();
          if (res) {
            const updatedItems = state.currDir.items.map((item) => {
              const size = sizes.get(item.name);
              return size !== undefined ? { ...item, size: size } : item;
            });
            /** @type {FileListDirUpdatedAction} */
            const action = {
              action: "FileListDirUpdatedAction",
              currDir: { ...state.currDir, items: updatedItems },
            };
            dispatch(action);
          }
        },
        () => {
          props.onClose();
          dispatch(TaskAction(Task("Viewing Items", resultP)));
        }
      );
    }

    useLayoutEffect(() => {
      // start scan
      viewItems(data.dispatch, data.actions, data.state);
    }, []);

    return h(statusPopupComp, {
      text: `Scanning the folder\n${currDir}`,
      title: "View",
      onClose: () => {
        // stop scan
        inProgress.current = false;
      },
    });
  };

  ViewItemsPopupComp.displayName = "ViewItemsPopup";

  return ViewItemsPopupComp;
}

ViewItemsPopup.statusPopupComp = StatusPopup;

export default ViewItemsPopup;
