/**
 * @typedef {import("@farjs/blessed").Widgets.Screen} BlessedScreen
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPanelProps } from "@farjs/filelist/FileListPanel.mjs"
 * @import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
 * @import {
 *  FileListDiskSpaceUpdatedAction,
 *  FileListDirChangedAction,
 * } from "@farjs/filelist/FileListActions.mjs"
 */
import React, { useLayoutEffect, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FileListEvent from "@farjs/filelist/FileListEvent.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPanel from "@farjs/filelist/FileListPanel.mjs";
import ZipApi from "./ZipApi.mjs";
import AddToArchController from "../AddToArchController.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly data: FileListData;
 *  readonly items: readonly FileListItem[];
 *  readonly move: boolean;
 * }} ZipPanelState
 */

/**
 * @param {string} zipPath
 * @param {string} rootPath
 * @param {Promise<Map<string, readonly FileListItem[]>>} entriesByParentP
 * @param {() => void} onClose
 * @returns
 */
const ZipPanel = (zipPath, rootPath, entriesByParentP, onClose) => {
  const { fileListPanelComp, addToArchController, messageBoxComp } = ZipPanel;

  /**
   * @param {FileListPanelProps} props
   */
  const ZipPanelImpl = (props) => {
    const stacks = WithStacks.useStacks();

    const [zipData, setZipData] = useState(
      /** @type {ZipPanelState | null} */ (null),
    );
    const [showWarning, setShowWarning] = useState(false);
    const theme = Theme.useTheme().popup;

    const onClosePanel = () => {
      const data = (() => {
        /** @type {PanelStackItem<FileListState>} */
        const stackItem = WithStacksProps.active(stacks).stack.peekLast();
        return stackItem.getData();
      })();
      if (data) {
        const { dispatch, actions, state } = data;
        dispatch(actions.updateDir(dispatch, state.currDir.path));
      }
      onClose();
    };

    /** @type {(screen: BlessedScreen, key: string) => boolean} */
    function onKeypress(_, key) {
      let processed = true;
      switch (key) {
        case "C-pageup":
          if (props.state.currDir.path === rootPath) {
            onClosePanel();
          } else processed = false;
          break;
        case "enter":
        case "C-pagedown":
          if (
            FileListState.currentItem(
              props.state,
              (i) => i.isDir && i.name === FileListItem.up.name,
            ) &&
            props.state.currDir.path === rootPath
          ) {
            onClosePanel();
          } else processed = false;
          break;
        case FileListEvent.onFileListCopy:
        case FileListEvent.onFileListMove:
          if (props.state.currDir.path !== rootPath) setShowWarning(true);
          else {
            const data = (() => {
              /** @type {PanelStackItem<FileListState>} */
              const stackItem = WithStacksProps.active(stacks).stack.peek();
              return stackItem.getData();
            })();
            if (data) {
              const { actions, state } = data;
              const items = (() => {
                if (state.selectedNames.size > 0) {
                  return FileListState.selectedItems(state);
                }

                const currItem = FileListState.currentItem(
                  state,
                  (_) => _ !== FileListItem.up,
                );
                return currItem ? [currItem] : [];
              })();

              if (actions.api.isLocal && items.length > 0) {
                setZipData({
                  data,
                  items,
                  move: key === FileListEvent.onFileListMove,
                });
              } else processed = false;
            }
          }
          break;
        default:
          processed = false;
          break;
      }

      return processed;
    }

    useLayoutEffect(() => {
      if (props.state.currDir.items.length === 0) {
        const zipP = entriesByParentP
          .then((entriesByParent) => {
            let total = 0;
            entriesByParent.forEach((entries) => {
              total += entries.reduce((res, i) => res + i.size, 0.0);
            });

            /** @type {FileListDiskSpaceUpdatedAction} */
            const diskSpaceUpdatedAction = {
              action: "FileListDiskSpaceUpdatedAction",
              diskSpace: total,
            };
            props.dispatch(diskSpaceUpdatedAction);

            /** @type {FileListDirChangedAction} */
            const dirChangedAction = {
              action: "FileListDirChangedAction",
              dir: FileListItem.currDir.name,
              currDir: FileListDir(
                rootPath,
                false,
                entriesByParent.get("") ?? [],
              ),
            };
            props.dispatch(dirChangedAction);
          })
          .catch((error) => {
            /** @type {FileListDirChangedAction} */
            const dirChangedAction = {
              action: "FileListDirChangedAction",
              dir: FileListItem.currDir.name,
              currDir: FileListDir(rootPath, false, []),
            };
            props.dispatch(dirChangedAction);
            return Promise.reject(error);
          });

        props.dispatch(TaskAction(Task("Reading zip archive", zipP)));
      }
    }, []);

    return h(
      React.Fragment,
      null,

      h(fileListPanelComp, { ...props, onKeypress }),

      showWarning
        ? h(messageBoxComp, {
            title: "Warning",
            message: "Items can only be added to zip root.",
            actions: [
              MessageBoxAction.OK(() => {
                setShowWarning(false);
              }),
            ],
            style: theme.regular,
          })
        : null,

      zipData !== null
        ? (() => {
            const { items, move } = zipData;
            const { dispatch, actions, state } = zipData.data;
            return h(addToArchController, {
              dispatch,
              actions,
              state,
              archName: zipPath,
              archType: "zip",
              archAction: move ? "Move" : "Copy",
              addToArchApi: ZipApi.addToZip,
              items,
              onComplete: () => {
                setZipData(null);

                const updateAction = props.actions.updateDir(
                  props.dispatch,
                  props.state.currDir.path,
                );
                props.dispatch(updateAction);

                if (move) {
                  updateAction.task.result.then(() => {
                    dispatch(
                      actions.deleteItems(dispatch, state.currDir.path, items),
                    );
                  });
                }
              },
              onCancel: () => {
                setZipData(null);
              },
            });
          })()
        : null,
    );
  };

  ZipPanelImpl.displayName = "ZipPanel";
  return ZipPanelImpl;
};

ZipPanel.fileListPanelComp = FileListPanel;
ZipPanel.addToArchController = AddToArchController;
ZipPanel.messageBoxComp = MessageBox;

export default ZipPanel;
