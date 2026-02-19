/**
 * @import {
 *  FileListDirUpdatedAction,
 *  FileListDiskSpaceUpdatedAction,
 * } from "@farjs/filelist/FileListActions.mjs"
 */
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListActions from "@farjs/filelist/FileListActions.mjs";
import ZipApi from "./ZipApi.mjs";

class ZipActions extends FileListActions {
  /**
   * @param {ZipApi} zipApi
   */
  constructor(zipApi) {
    super(zipApi);

    /** @private @readonly @type {ZipApi} */
    this.zipApi = zipApi;
  }

  /** @type {FileListActions['updateDir']} */
  updateDir(dispatch, path) {
    const entriesByParentP = ZipActions.readZip(this.zipApi.zipPath).then(
      (entries) => {
        let totalSize = 0;
        for (const [_, items] of entries) {
          totalSize += items.reduce((total, i) => total + i.size, 0);
        }
        /** @type {FileListDiskSpaceUpdatedAction} */
        const action = {
          action: "FileListDiskSpaceUpdatedAction",
          diskSpace: totalSize,
        };
        dispatch(action);
        return entries;
      },
    );

    this.api = ZipActions.createApi(
      this.zipApi.zipPath,
      this.zipApi.rootPath,
      entriesByParentP,
    );

    const updateP = entriesByParentP
      .then(() => this.api.readDir(path))
      .then((currDir) => {
        /** @type {FileListDirUpdatedAction} */
        const action = { action: "FileListDirUpdatedAction", currDir };
        dispatch(action);
        return currDir;
      });

    return TaskAction(Task("Updating Dir", updateP));
  }

  static readZip = ZipApi.readZip;

  static createApi = ZipApi.create;
}

export default ZipActions;
