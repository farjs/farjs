/**
 * @typedef {import("@farjs/filelist/stack/WithStacksProps.mjs").WithStacksProps} WithStacksProps
 * @import { ArchiverPluginUiParams } from "./ArchiverPluginUi.mjs"
 */
import path from "path";
import FileListDir from "@farjs/filelist/api/FileListDir.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPanelController from "@farjs/filelist/FileListPanelController.mjs";
import ZipApi from "./zip/ZipApi.mjs";
import ZipActions from "./zip/ZipActions.mjs";
import ZipPanel from "./zip/ZipPanel.mjs";
import ArchiverPluginUi from "./ArchiverPluginUi.mjs";

class ArchiverPlugin extends FileListPlugin {
  constructor() {
    super();
  }

  /** @type {FileListPlugin['onKeyTrigger']} */
  async onKeyTrigger(_, stacks) {
    const uiParams = this._createUiParams(stacks);
    return uiParams ? ArchiverPluginUi(uiParams) : undefined;
  }

  /** @type {FileListPlugin['onFileTrigger']} */
  async onFileTrigger(filePath, fileHeader, onClose) {
    const pathLower = filePath.toLowerCase();
    if (
      pathLower.endsWith(".zip") ||
      pathLower.endsWith(".jar") ||
      this._checkFileHeader(fileHeader)
    ) {
      const fileName = path.parse(filePath).base;
      const rootPath = `zip://${fileName}`;
      const entriesByParentF = ZipActions.readZip(filePath);

      return new PanelStackItem(
        FileListPanelController(
          ZipPanel(filePath, rootPath, entriesByParentF, onClose),
        ),
        undefined,
        new ZipActions(
          ZipActions.createApi(filePath, rootPath, entriesByParentF),
        ),
        { ...FileListState(), currDir: FileListDir(rootPath, false, []) },
      );
    }

    return undefined;
  }

  /**
   * @param {WithStacksProps} stacks
   * @returns {ArchiverPluginUiParams | undefined}
   */
  _createUiParams(stacks) {
    const stackItem = WithStacksProps.active(stacks).stack.peek();
    const data = stackItem.getData();
    if (data) {
      const { actions, state } = data;
      const items = (() => {
        if (state.selectedNames.size > 0) {
          return FileListState.selectedItems(state);
        }

        const currItem = FileListState.currentItem(
          state,
          (_) => _ !== FileListItem.up,
        );
        return currItem ? [currItem] : [];
      })();

      if (actions.api.isLocal && items.length > 0) {
        const zipName = `${items[0].name}.zip`;
        return {
          data,
          archName: zipName,
          archType: "zip",
          addToArchApi: ZipApi.addToZip,
          items,
        };
      }
    }

    return undefined;
  }

  /** @private @type {(header: Uint8Array) => boolean} */
  _checkFileHeader(header) {
    if (header.length < 4) {
      return false;
    }

    return (
      header[0] === P &&
      header[1] === K &&
      header[2] === 0x03 &&
      header[3] === 0x04
    );
  }
}

const P = "P".charCodeAt(0);
const K = "K".charCodeAt(0);

export default new ArchiverPlugin();
