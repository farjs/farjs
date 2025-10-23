/**
 * @typedef {import("@farjs/filelist/api/FileListItem.mjs").FileListItem} FileListItem
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 */
import path from "path";
import React, { useLayoutEffect, useRef, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FileExistsPopup from "./FileExistsPopup.mjs";
import CopyProgressPopup from "./CopyProgressPopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  setInterval(callback: () => void, ms: number): NodeJS.Timer;
 *  clearInterval(intervalId: NodeJS.Timer): void;
 * }} Timers
 */

/**
 * @typedef {{
 *  readonly from: FileListData;
 *  readonly to: FileListData;
 *  readonly move: boolean;
 *  readonly fromPath: string;
 *  readonly items: readonly CopyProcessItem[];
 *  readonly toPath: string;
 *  readonly total: number;
 *  onTopItem(topItem: FileListItem): void;
 *  onDone(): void;
 * }} CopyProcessProps
 */

/**
 * @typedef {{
 *  readonly item: FileListItem;
 *  readonly toName: string;
 * }} CopyProcessItem
 */

/**
 * @typedef {{
 *  readonly time100ms: number;
 *  readonly cancel: boolean;
 *  readonly existing?: FileListItem;
 * }} CopyState
 */

/**
 * @typedef {{
 *  readonly item: FileListItem;
 *  readonly to: string;
 *  readonly itemPercent: number;
 *  readonly itemBytes: number;
 *  readonly total: number;
 *  readonly askWhenExists: boolean;
 * }} CopyInfo
 */

/**
 * @param {CopyProcessProps} props
 */
const CopyProcess = (props) => {
  const { copyProgressPopup, fileExistsPopup, messageBoxComp } = CopyProcess;

  const [state, setState] = useState(
    /** @type {() => CopyState} */ () => ({ time100ms: 0, cancel: false })
  );
  const inProgress = useRef(false);
  const cancelPromise = useRef({
    /** @type {Promise<void>} */
    p: Promise.resolve(),
    /** @type {(args: void) => void} */
    resolve: () => {},
  });
  const existsPromise = useRef({
    /** @type {Promise<boolean | undefined>} */
    p: Promise.resolve(undefined),
    /** @type {(v: boolean | undefined) => void} */
    resolve: () => {},
  });
  /** @type {React.MutableRefObject<CopyInfo>} */
  const data = useRef({
    item: FileListItem(""),
    to: "",
    itemPercent: 0,
    itemBytes: 0,
    total: 0,
    askWhenExists: true,
  });
  const currTheme = Theme.useTheme();

  const doCopy = () => {
    /**
     * @param {boolean} copied
     * @param {string} parent
     * @param {string} targetDir
     * @param {readonly CopyProcessItem[]} items
     * @returns {Promise<boolean[]>}
     */
    function loop(copied, parent, targetDir, items) {
      return items.reduce(async (resP, cpItem) => {
        const [prevCopied, prevInProgress] = await resP;
        const { item, toName } = cpItem;
        if (item.isDir && prevInProgress && inProgress.current) {
          const dirList = await props.from.actions.api.readDir(
            parent,
            item.name
          );
          const dstDir = await props.to.actions.api.mkDirs([targetDir, toName]);
          const [isCopied, done] = await loop(
            prevCopied,
            dirList.path,
            dstDir,
            dirList.items.map((i) => ({ item: i, toName: i.name }))
          );
          if (isCopied && done && props.move) {
            await props.from.actions.api.delete(parent, [item]);
          }
          return [isCopied, done];
        }
        if (!item.isDir && prevInProgress && inProgress.current) {
          data.current = {
            ...data.current,
            item: item,
            to: path.join(targetDir, toName),
            itemPercent: 0,
            itemBytes: 0,
          };
          let isCopied = true;
          const done = await props.from.actions.copyFile(
            parent,
            item,
            props.to.actions.api.writeFile(
              targetDir,
              toName,
              async (existing) => {
                if (inProgress.current && data.current.askWhenExists) {
                  setState((s) => ({ ...s, existing }));
                  /** @type {(val: boolean | undefined) => void} */
                  let resolve = () => {};
                  const p = new Promise((res) => (resolve = res));
                  existsPromise.current = { p, resolve };
                }
                const maybeOverwrite = await existsPromise.current.p;
                if (maybeOverwrite === undefined) {
                  isCopied = false;
                }
                return maybeOverwrite;
              }
            ),
            async (position) => {
              data.current = {
                ...data.current,
                itemPercent: Math.trunc(divide(position, item.size) * 100),
                itemBytes: position,
              };
              await cancelPromise.current.p;
              return inProgress.current;
            }
          );
          if (isCopied && done && props.move) {
            await props.from.actions.api.delete(parent, [item]);
          }
          if (done) {
            const d = data.current;
            data.current = {
              ...data.current,
              itemBytes: 0,
              total: d.total + d.itemBytes,
            };
          }
          return [prevCopied && isCopied, done];
        }
        return [prevCopied, prevInProgress];
      }, Promise.resolve([copied, inProgress.current]));
    }

    const resultP = props.items.reduce(async (resP, topItem) => {
      const res = await resP;
      if (res && inProgress.current) {
        const [isCopied, done] = await loop(
          true,
          props.fromPath,
          props.toPath,
          [topItem]
        );
        if (isCopied && done) {
          props.onTopItem(topItem.item);
        }
        return done;
      }
      return res;
    }, Promise.resolve(true));

    resultP.then(
      () => props.onDone(),
      () => {
        props.onDone();
        props.from.dispatch(TaskAction(Task("Copy/Move Items", resultP)));
      }
    );
  };

  useLayoutEffect(() => {
    const timerId = CopyProcess.timers.setInterval(() => {
      setState((s) => {
        return !s.cancel
          ? {
              ...s,
              time100ms: s.time100ms + 1,
            }
          : s;
      });
    }, 100);

    inProgress.current = true;
    doCopy();

    const cleanup = () => {
      inProgress.current = false;
      CopyProcess.timers.clearInterval(timerId);
    };
    return cleanup;
  }, []);

  const d = data.current;
  const timeSeconds = Math.trunc(divide(state.time100ms, 10));
  const bytesPerSecond = Math.trunc(divide(d.total + d.itemBytes, timeSeconds));
  const existing = state.existing;

  return h(
    React.Fragment,
    null,
    h(copyProgressPopup, {
      move: props.move,
      item: d.item.name,
      to: d.to,
      itemPercent: d.itemPercent,
      total: props.total,
      totalPercent: Math.trunc(
        divide(d.total + d.itemBytes, props.total) * 100
      ),
      timeSeconds: timeSeconds,
      leftSeconds: Math.trunc(
        divide(
          Math.max(props.total - (d.total + d.itemBytes), 0),
          bytesPerSecond
        )
      ),
      bytesPerSecond,
      onCancel: () => {
        setState((s) => ({ ...s, cancel: true }));
        /** @type {(val: any) => void} */
        let resolve = () => {};
        const p = new Promise((res) => (resolve = res));
        cancelPromise.current = { p, resolve };
      },
    }),

    existing !== undefined
      ? h(fileExistsPopup, {
          newItem: d.item,
          existing: existing,
          onAction: (action) => {
            setState((s) => ({ ...s, existing: undefined }));

            if (action === "All" || action === "SkipAll") {
              data.current = { ...data.current, askWhenExists: false };
            }
            switch (action) {
              case "Overwrite":
              case "All":
                existsPromise.current.resolve(true);
                break;
              case "Skip":
              case "SkipAll":
                existsPromise.current.resolve(undefined);
                break;
              case "Append":
              default:
                existsPromise.current.resolve(false);
                break;
            }
          },
          onCancel: () => {
            setState((s) => ({ ...s, existing: undefined }));
            inProgress.current = false;
            existsPromise.current.resolve(undefined);
          },
        })
      : null,

    state.cancel
      ? h(messageBoxComp, {
          title: "Operation has been interrupted",
          message: "Do you really want to cancel it?",
          actions: [
            MessageBoxAction.YES(() => {
              setState((s) => ({ ...s, cancel: false }));
              inProgress.current = false;
              cancelPromise.current.resolve();
            }),
            MessageBoxAction.NO(() => {
              setState((s) => ({ ...s, cancel: false }));
              cancelPromise.current.resolve();
            }),
          ],
          style: currTheme.popup.error,
        })
      : null
  );
};

/** @type {(x: number, y: number) => number} */
function divide(x, y) {
  return y === 0 ? 0 : x / y;
}

CopyProcess.displayName = "CopyProcess";
CopyProcess.copyProgressPopup = CopyProgressPopup;
CopyProcess.fileExistsPopup = FileExistsPopup;
CopyProcess.messageBoxComp = MessageBox;
/** @type {Timers} */
CopyProcess.timers = { setInterval, clearInterval };

export default CopyProcess;
