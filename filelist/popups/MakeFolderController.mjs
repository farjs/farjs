/**
 * @typedef {import("../FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import MakeFolderPopup from "./MakeFolderPopup.mjs";

const h = React.createElement;

let initialMultiple = false;

/**
 * @param {FileListUiData} props
 */
const MakeFolderController = (props) => {
  const { makeFolderPopup } = MakeFolderController;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const data = props.data;

  if (data && props.showMkFolderPopup) {
    /** @type {(dir: string, multiple: boolean) => Promise<void>} */
    const saveHistory = async (dir, multiple) => {
      const mkDirsHistory = await historyProvider.get(
        MakeFolderPopup.mkDirsHistoryKind
      );
      await mkDirsHistory.save({ item: dir });

      initialMultiple = multiple;
      props.onClose();
    };

    return h(makeFolderPopup, {
      multiple: initialMultiple,
      onOk: (dir, multiple) => {
        const action = data.actions.createDir(
          data.dispatch,
          data.state.currDir.path,
          dir,
          multiple
        );
        const a = {
          ...action,
          task: {
            ...action.task,
            result: action.task.result.then(async (res) => {
              await saveHistory(dir, multiple);
              return res;
            }),
          },
        };
        data.dispatch(a);
      },
      onCancel: props.onClose,
    });
  }

  return null;
};

MakeFolderController.displayName = "MakeFolderController";
MakeFolderController.makeFolderPopup = MakeFolderPopup;

export default MakeFolderController;
