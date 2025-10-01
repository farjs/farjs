/**
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 */
import React, { useLayoutEffect, useRef, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly actions: FileListActions;
 *  readonly fromPath: string;
 *  readonly items: readonly FileListItem[];
 *  readonly title: string;
 *  onDone(total: number): void;
 *  onCancel(): void;
 * }} CopyItemsStatsProps
 */

/**
 * @param {CopyItemsStatsProps} props
 */
const CopyItemsStats = (props) => {
  const { statusPopupComp } = CopyItemsStats;

  const [currDir, setCurrDir] = useState("");
  const inProgress = useRef(false);

  const scanDir = () => {
    const parent = props.fromPath;
    let filesSize = 0;

    const resultP = props.items.reduce(async (resP, currItem) => {
      const res = await resP;
      if (res && currItem.isDir && inProgress.current) {
        setCurrDir(currItem.name);
        return props.actions.scanDirs(parent, [currItem], (_, items) => {
          filesSize += items.reduce((res, i) => {
            return res + (i.isDir ? 0 : i.size);
          }, 0);
          return inProgress.current;
        });
      }
      if (res && !currItem.isDir) {
        filesSize += currItem.size;
        return true;
      }
      return res;
    }, Promise.resolve(true));

    resultP.then(
      (res) => {
        if (res) {
          props.onDone(filesSize);
        }
        //else: already cancelled
      },
      () => {
        props.onCancel();
        props.dispatch(TaskAction(Task(`${props.title} dir scan`, resultP)));
      }
    );
  };

  useLayoutEffect(() => {
    // start scan
    inProgress.current = true;
    scanDir();

    const cleanup = () => {
      // stop scan
      inProgress.current = false;
    };
    return cleanup;
  }, []);

  return h(statusPopupComp, {
    text: `Calculating total size\n${currDir}`,
    title: props.title,
    onClose: props.onCancel,
  });
};

CopyItemsStats.displayName = "CopyItemsStats";
CopyItemsStats.statusPopupComp = StatusPopup;

export default CopyItemsStats;
