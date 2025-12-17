/**
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListItemCreatedAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs"
 */
import React from "react";
import AddToArchController from "./AddToArchController.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly data: FileListData;
 *  readonly archName: string;
 *  readonly archType: string;
 *  addToArchApi(zipFile: string, parent: string, items: Set<string>, onNextItem: () => void): Promise<void>;
 *  readonly items: readonly FileListItem[];
 * }} ArchiverPluginUiParams
 */

/**
 * @param {ArchiverPluginUiParams} params
 */
function ArchiverPluginUi({ data, archName, archType, addToArchApi, items }) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const ArchiverPluginUiComp = (props) => {
    const { addToArchController } = ArchiverPluginUi;

    return h(addToArchController, {
      dispatch: data.dispatch,
      actions: data.actions,
      state: data.state,
      archName,
      archType,
      archAction: "Add",
      addToArchApi: addToArchApi,
      items,
      onComplete: (archFile) => {
        props.onClose();

        const action = data.actions.updateDir(
          data.dispatch,
          data.state.currDir.path
        );
        data.dispatch(action);
        action.task.result.then((updatedDir) => {
          /** @type {FileListItemCreatedAction} */
          const action = {
            action: "FileListItemCreatedAction",
            name: archFile,
            currDir: updatedDir,
          };
          data.dispatch(action);
        });
      },
      onCancel: props.onClose,
    });
  };

  ArchiverPluginUiComp.displayName = "ArchiverPluginUi";

  return ArchiverPluginUiComp;
}

ArchiverPluginUi.addToArchController = AddToArchController;

export default ArchiverPluginUi;
