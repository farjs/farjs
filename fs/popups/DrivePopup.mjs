/**
 * @import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs"
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListState } from "@farjs/filelist/FileListState.mjs"
 * @import { FSDisk } from "../FSDisk.mjs"
 * @typedef {import("../FSService.mjs").FSService} FSService
 */
import React, { useLayoutEffect, useState } from "react";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import MenuPopup from "@farjs/ui/menu/MenuPopup.mjs";
import SingleChars from "@farjs/ui/border/SingleChars.mjs";
import WithSize from "@farjs/ui/WithSize.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import FSService from "../FSService.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly showOnLeft: boolean;
 *  onChangeDir(dir: string): void;
 *  onClose(): void;
 * }} DrivePopupProps
 */

/**
 * @param {DrivePopupProps} props
 */
const DrivePopup = (props) => {
  const { platform, fsService, withSizeComp, menuPopup } = DrivePopup;

  const [disks, setDisks] = useState(/** @type {readonly FSDisk[]} */ ([]));

  const stacks = WithStacks.useStacks();
  const data = getData(platform, disks);

  const [panelInput, currStack, otherStack] = props.showOnLeft
    ? [stacks.left.input, stacks.left.stack, stacks.right.stack]
    : [stacks.right.input, stacks.right.stack, stacks.left.stack];

  /** @type {(dir: string) => void} */
  function onAction(dir) {
    /**
     * @returns {string}
     */
    function getTargetDir() {
      /** @type {PanelStackItem<FileListState>} */
      const otherStackItem = otherStack.peekLast();
      const otherState = otherStackItem.state;
      if (otherState && otherState.currDir.path.startsWith(dir)) {
        return otherState.currDir.path;
      }

      /** @type {PanelStackItem<FileListState>} */
      const currStackItem = currStack.peekLast();
      const currState = currStackItem.state;
      if (currState && currState.currDir.path.startsWith(dir)) {
        return currState.currDir.path;
      }

      return dir;
    }

    props.onChangeDir(getTargetDir());
  }

  useLayoutEffect(() => {
    const disksP = fsService.readDisks().then((disks) => {
      setDisks(disks);
    });
    props.dispatch(TaskAction(Task("Reading disks", disksP)));
  }, []);

  return data.length > 0
    ? h(withSizeComp, {
        render: () => {
          return h(menuPopup, {
            title: "Drive",
            items: data.map((_) => _.item),
            getLeft: (width) => {
              const panelWidth =
                panelInput && panelInput.width
                  ? /** @type {number} */ (panelInput.width)
                  : 0;

              return MenuPopup.getLeftPos(panelWidth, props.showOnLeft, width);
            },
            onSelect: (index) => {
              onAction(data[index].dir);
            },
            onClose: props.onClose,
          });
        },
      })
    : null;
};

DrivePopup.displayName = "DrivePopup";
/** @type {NodeJS.Platform} */
DrivePopup.platform = process.platform;
/** @type {FSService} */
DrivePopup.fsService = FSService.instance;
DrivePopup.withSizeComp = WithSize;
DrivePopup.menuPopup = MenuPopup;

/**
 * @param {NodeJS.Platform} platform
 * @param {readonly FSDisk[]} disks
 * @returns {readonly {
 *  readonly dir: string;
 *  readonly item: string;
 * }[]}
 */
function getData(platform, disks) {
  if (disks.length === 0) {
    return [];
  }

  /**
   * @type {readonly {
   *  readonly dir: string;
   *  readonly name: string;
   *  readonly size: string;
   *  readonly free: string;
   * }[]}
   */
  const items = disks.map((d) => {
    return {
      dir: d.root,
      name: d.name,
      size: DrivePopup._toCompact(d.size),
      free: DrivePopup._toCompact(d.free),
    };
  });
  const maxNameWidth = 15;
  const maxSizeWidth = items
    .map((_) => _.size.length)
    .reduce((_1, _2) => (_1 > _2 ? _1 : _2), 0);
  const maxFreeWidth = items
    .map((_) => _.free.length)
    .reduce((_1, _2) => (_1 > _2 ? _1 : _2), 0);
  const sep = SingleChars.vertical;

  return items.map(({ dir, name: iName, size: iSize, free: iFree }) => {
    const name = iName
      .slice(0, Math.min(maxNameWidth, iName.length))
      .padEnd(maxNameWidth);
    const size = `${" ".repeat(maxSizeWidth - iSize.length)}${iSize}`;
    const free = `${" ".repeat(maxFreeWidth - iFree.length)}${iFree}`;

    return platform === "win32"
      ? {
          dir,
          item: `  ${dir} ${sep}${name}${sep}${size}${sep}${free} `,
        }
      : { dir, item: ` ${name}${sep}${size}${sep}${free} ` };
  });
}

const kBytes = 1024;
const mBytes = 1024 * kBytes;
const gBytes = 1024 * mBytes;

/**
 * @type {(bytes: number) => string}
 */
DrivePopup._toCompact = (bytes) => {
  if (bytes === 0) {
    return "";
  }

  const [size, mod] = (() => {
    return bytes > 1000 * gBytes
      ? [bytes / gBytes, " G"]
      : bytes > 1000 * mBytes
      ? [bytes / mBytes, " M"]
      : bytes > 1000 * kBytes
      ? [bytes / kBytes, " K"]
      : [bytes, ""];
  })();

  return `${Math.round(size)}${mod}`;
};

export default DrivePopup;
