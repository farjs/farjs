/**
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListDir } from "@farjs/filelist/api/FileListDir.mjs"
 * @import { FileListAction } from "@farjs/filelist/FileListActions.mjs"
 * @typedef {import("./FSService.mjs").FSService} FSService
 */
import React, { useLayoutEffect, useRef } from "react";
import FSService from "./FSService.mjs";

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly currDir: FileListDir;
 * }} FSFreeSpaceProps
 */

/**
 * @param {FSFreeSpaceProps} props
 */
const FSFreeSpace = (props) => {
  const { fsService } = FSFreeSpace;

  const currDirRef = /** @type {React.MutableRefObject<FileListDir>} */ (
    useRef()
  );
  currDirRef.current = props.currDir;

  useLayoutEffect(() => {
    const currDir = props.currDir;
    fsService.readDisk(currDir.path).then(
      (disk) => {
        if (disk && currDir === currDirRef.current) {
          /** @type {FileListAction} */
          const action = {
            action: "FileListDiskSpaceUpdatedAction",
            diskSpace: disk.free,
          };
          props.dispatch(action);
        }
      },
      () => undefined,
    );
  }, [props.currDir]);

  return null;
};

FSFreeSpace.displayName = "FSFreeSpace";
/** @type {FSService} */
FSFreeSpace.fsService = FSService.instance;

export default FSFreeSpace;
