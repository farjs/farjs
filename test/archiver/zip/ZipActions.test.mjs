/**
 * @import {
 *  FileListDirUpdatedAction,
 *  FileListDiskSpaceUpdatedAction,
 * } from "@farjs/filelist/FileListActions.mjs"
 */
import { deepEqual } from "node:assert/strict";
import mockFunction from "mock-fn";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import ZipApi from "../../../archiver/zip/ZipApi.mjs";
import ZipEntry from "../../../archiver/zip/ZipEntry.mjs";
import ZipActions from "../../../archiver/zip/ZipActions.mjs";

const { describe, it } = await (async () => {
  // @ts-ignore
  const module = process.isBun ? "bun:test" : "node:test";
  // @ts-ignore
  return process.isBun // @ts-ignore
    ? Promise.resolve({ describe: (_, fn) => fn(), it: test })
    : import(module);
})();

describe("ZipActions.test.mjs", () => {
  it("should re-create ZipApi and dispatch actions when updateDir", async () => {
    //given
    let dispatchArgs = /** @type {any[]} */ ([]);
    const dispatch = mockFunction((...args) => dispatchArgs.push(args));
    let readZipArgs = /** @type {any[]} */ ([]);
    const readZip = mockFunction((...args) => {
      readZipArgs = args;
      return entriesByParentP;
    });
    let createApiArgs = /** @type {any[]} */ ([]);
    const createApi = mockFunction((...args) => {
      createApiArgs = args;
      return api;
    });
    ZipActions.readZip = readZip;
    ZipActions.createApi = createApi;
    const actions = new ZipActions(
      new ZipApi("file.zip", "root.path", Promise.resolve(new Map())),
    );
    const currDir = FileListDir("/", true, [FileListItem("file 1")]);
    const path = "/test/path";
    class TestZipApi extends ZipApi {
      constructor() {
        super("file.zip", "root.path", Promise.resolve(new Map()));
        this.readDir = () => Promise.resolve(currDir);
      }
    }
    const api = new TestZipApi();
    const entriesByParentP = Promise.resolve(
      new Map([
        ["", [ZipEntry("", "file 1", false, 100), ZipEntry("", "dir 1", true)]],
        ["dir 1", [ZipEntry("dir 1", "file 2", false, 23)]],
      ]),
    );

    //when
    const { task } = actions.updateDir(dispatch, path);

    //then
    deepEqual(actions.api === api, true);
    deepEqual(task.message, "Updating Dir");
    deepEqual(await task.result, currDir);

    deepEqual(readZip.times, 1);
    deepEqual(readZipArgs, ["file.zip"]);
    deepEqual(createApi.times, 1);
    deepEqual(createApiArgs.slice(0, 2), ["file.zip", "root.path"]);
    deepEqual(await createApiArgs[2], await entriesByParentP);

    /** @type {FileListDiskSpaceUpdatedAction} */
    const spaceUpdatedAction = {
      action: "FileListDiskSpaceUpdatedAction",
      diskSpace: 123,
    };
    /** @type {FileListDirUpdatedAction} */
    const dirUpdatedAction = {
      action: "FileListDirUpdatedAction",
      currDir,
    };
    deepEqual(dispatch.times, 2);
    deepEqual(dispatchArgs, [[spaceUpdatedAction], [dirUpdatedAction]]);
  });
});
