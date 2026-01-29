/**
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs";
 */
import React from "react";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import DriveController from "./popups/DriveController.mjs";
import FoldersHistoryController from "./popups/FoldersHistoryController.mjs";
import FolderShortcutsController from "./popups/FolderShortcutsController.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly showDrivePopupOnLeft?: boolean;
 *  readonly showFoldersHistoryPopup: boolean;
 *  readonly showFolderShortcutsPopup: boolean;
 * }} FSPluginUiOptions
 */

/**
 * @param {FSPluginUiOptions} options
 */
function FSPluginUi(
  {
    showDrivePopupOnLeft,
    showFoldersHistoryPopup,
    showFolderShortcutsPopup,
  } = {
    showDrivePopupOnLeft: undefined,
    showFoldersHistoryPopup: false,
    showFolderShortcutsPopup: false,
  },
) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const FSPluginUiComp = (props) => {
    const { drive, foldersHistory, folderShortcuts } = FSPluginUi;

    const stacks = WithStacks.useStacks();

    /** @type {(isLeft: boolean) => (dir: string) => void} */
    function onChangeDir(isLeft) {
      return (dir) => {
        const currStack = isLeft ? stacks.left.stack : stacks.right.stack;

        if (currStack.peek() !== currStack.peekLast()) {
          currStack.clear();
        }

        const stackItem = currStack.peekLast();
        const data = stackItem.getData();
        if (data) {
          const { dispatch, actions, state } = data;
          if (dir != state.currDir.path) {
            dispatch(actions.changeDir(dispatch, "", dir));
          }
        }
      };
    }

    const onChangeDirInActivePanel = onChangeDir(stacks.left.stack.isActive);

    return h(
      React.Fragment,
      null,

      h(drive, {
        dispatch: props.dispatch,
        showDrivePopupOnLeft,
        onChangeDir: (dir, isLeft) => {
          onChangeDir(isLeft)(dir);
        },
        onClose: props.onClose,
      }),

      h(foldersHistory, {
        showPopup: showFoldersHistoryPopup,
        onChangeDir: onChangeDirInActivePanel,
        onClose: props.onClose,
      }),

      h(folderShortcuts, {
        showPopup: showFolderShortcutsPopup,
        onChangeDir: onChangeDirInActivePanel,
        onClose: props.onClose,
      }),
    );
  };

  FSPluginUiComp.displayName = "FSPluginUi";

  return FSPluginUiComp;
}

FSPluginUi.drive = DriveController;
FSPluginUi.foldersHistory = FoldersHistoryController;
FSPluginUi.folderShortcuts = FolderShortcutsController;

export default FSPluginUi;
