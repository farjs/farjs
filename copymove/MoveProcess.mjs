/**
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 */
import path from "path";
import nodeFs from "fs";
import fsPromises from "fs/promises";
import React, { useLayoutEffect, useRef, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  existsSync(path: string): boolean;
 *  rename(oldPath: string, newPath: string): Promise<void>;
 * }} FS
 */

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly actions: FileListActions;
 *  readonly fromPath: string;
 *  readonly items: readonly {
 *    readonly item: FileListItem;
 *    readonly toName: string;
 *  }[];
 *  readonly toPath: string;
 *  onTopItem(topItem: FileListItem): void;
 *  onDone(): void;
 * }} MoveProcessProps
 */

/**
 * @typedef {{
 *  readonly currItem: string;
 *  readonly existing?: string;
 * }} MoveState
 */

/**
 * @param {MoveProcessProps} props
 */
const MoveProcess = (props) => {
  const { statusPopupComp, messageBoxComp } = MoveProcess;

  const [state, setState] = useState(
    () => /** @type {MoveState} */ ({ currItem: "" })
  );
  const inProgress = useRef(false);
  const existsPromise = useRef({
    /** @type {Promise<boolean>} */
    p: Promise.resolve(true),
    /** @type {(v: boolean) => void} */
    resolve: () => {},
  });
  const askWhenExists = useRef(true);
  const currTheme = Theme.useTheme();

  const moveItems = () => {
    const resultP = props.items.reduce(async (resP, cpItem) => {
      const res = await resP;
      const { item: currItem, toName } = cpItem;
      if (res && inProgress.current) {
        setState((s) => {
          return { ...s, currItem: currItem.name };
        });
        const oldPath = path.join(props.fromPath, currItem.name);
        const newPath = path.join(props.toPath, toName);
        const exists = !currItem.isDir && MoveProcess.fs.existsSync(newPath);

        if (exists && askWhenExists.current) {
          setState((s) => {
            return { ...s, existing: newPath };
          });
          /** @type {(v: boolean) => void} */
          let resolve = () => {};
          const p = new Promise((res) => (resolve = res));
          existsPromise.current = { p, resolve };
        }

        const overwrite = await existsPromise.current.p;
        if (!exists || overwrite) {
          await MoveProcess.fs.rename(oldPath, newPath);
          props.onTopItem(currItem);
          return inProgress.current;
        }
        return inProgress.current;
      }
      return res;
    }, Promise.resolve(true));

    resultP.then(
      () => props.onDone(),
      () => {
        props.onDone();
        props.dispatch(TaskAction(Task("Moving items", resultP)));
      }
    );
  };

  /** @type {(overwrite: boolean, all?: boolean, cancel?: boolean) => () => void} */
  function onExistsAction(overwrite, all = false, cancel = false) {
    return () => {
      setState((s) => {
        return { ...s, existing: undefined };
      });
      askWhenExists.current = !all;
      inProgress.current = !cancel;
      existsPromise.current.resolve(overwrite);
    };
  }

  useLayoutEffect(() => {
    // start
    inProgress.current = true;
    moveItems();
  }, []);

  const existing = state.existing;

  return h(
    React.Fragment,
    null,
    h(statusPopupComp, {
      text: `Moving item\n${state.currItem}`,
      title: "Move",
      onClose: () => {
        // stop
        inProgress.current = false;
      },
    }),

    existing !== undefined
      ? h(messageBoxComp, {
          title: "Warning",
          message: `File already exists.\nDo you want to overwrite it's content?\n\n${existing}`,
          actions: [
            MessageBoxAction("Overwrite")(onExistsAction(true)),
            MessageBoxAction("All")(onExistsAction(true, true)),
            MessageBoxAction("Skip")(onExistsAction(false)),
            MessageBoxAction("Skip all")(onExistsAction(false, true)),
            MessageBoxAction("Cancel", true)(onExistsAction(false, true, true)),
          ],
          style: currTheme.popup.error,
        })
      : null
  );
};

MoveProcess.displayName = "MoveProcess";
MoveProcess.statusPopupComp = StatusPopup;
MoveProcess.messageBoxComp = MessageBox;
/** @type {FS} */
MoveProcess.fs = { ...nodeFs, ...fsPromises };

export default MoveProcess;
