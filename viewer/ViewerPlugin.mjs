/**
 * @typedef {import("@farjs/filelist/stack/WithStacksProps.mjs").WithStacksProps} WithStacksProps
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileViewHistory } from "../file/FileViewHistory.mjs"
 */
import fs from "fs";
import path from "path";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileEvent from "../file/FileEvent.mjs";
import ViewerEvent from "./ViewerEvent.mjs";
import ViewItemsPopup from "./ViewItemsPopup.mjs";
import ViewerPluginUi from "./ViewerPluginUi.mjs";

class ViewerPluginImpl extends FileListPlugin {
  constructor() {
    super([
      "f3",
      ViewerEvent.onViewerOpenLeft,
      ViewerEvent.onViewerOpenRight,
      FileEvent.onFileView,
    ]);
  }

  /**
   * @param {string} key
   * @param {WithStacksProps} stacks
   * @param {any} [data]
   * @returns {Promise<ReactComponent | undefined>}
   */
  async onKeyTrigger(key, stacks, data) {
    if (key === FileEvent.onFileView) {
      if (data) {
        /** @type {FileViewHistory} */
        const history = data;
        const size = fs.lstatSync(history.path).size;
        return ViewerPluginUi(history.path, size);
      }

      return undefined;
    }

    const stack = (() => {
      switch (key) {
        case ViewerEvent.onViewerOpenLeft:
          return stacks.left.stack;
        case ViewerEvent.onViewerOpenRight:
          return stacks.right.stack;
        default:
          return WithStacksProps.active(stacks).stack;
      }
    })();

    /** @type {PanelStackItem<FileListState>} */
    const stackItem = stack.peek();
    const fileListData = stackItem.getData();
    if (fileListData) {
      const { actions, state } = fileListData;
      const item = FileListState.currentItem(
        state,
        (_) => _ !== FileListItem.up
      );

      if (item && actions.api.isLocal && !item.isDir) {
        const filePath = path.join(state.currDir.path, item.name);
        const size = fs.lstatSync(filePath).size;
        return ViewerPluginUi(filePath, size);
      }

      if (state.selectedNames.size > 0 || (item && item.isDir)) {
        return ViewItemsPopup(fileListData);
      }
    }

    return undefined;
  }
}

const ViewerPlugin = new ViewerPluginImpl();

export default ViewerPlugin;
