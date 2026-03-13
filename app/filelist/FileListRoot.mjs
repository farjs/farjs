/**
 * @import { Dispatch, ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs"
 */
import React from "react";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import ArchiverPlugin from "../../archiver/ArchiverPlugin.mjs";
import CopyMovePlugin from "../../copymove/CopyMovePlugin.mjs";
import ViewerPlugin from "../../viewer/ViewerPlugin.mjs";
import QuickViewPlugin from "../../viewer/quickview/QuickViewPlugin.mjs";
import FSPlugin from "../../fs/FSPlugin.mjs";
import FSServices from "../../fs/FSServices.mjs";
import FilePlugin from "../../file/FilePlugin.mjs";
import FileListUiPlugin from "../../filelist/FileListUiPlugin.mjs";
import FileListBrowser from "./FileListBrowser.mjs";
import FileListModule from "./FileListModule.mjs";
import FileListPluginHandler from "./FileListPluginHandler.mjs";

const h = React.createElement;

/**
 * @param {Dispatch} dispatch
 * @param {FileListModule} module
 * @param {ReactComponent} withPortalsComp
 */
function FileListRoot(
  dispatch,
  { historyProvider, fsServices },
  withPortalsComp,
) {
  /**
   * @param {React.PropsWithChildren<any>} props
   */
  const FileListRootComp = (props) => {
    const { fileListComp } = FileListRoot;

    return h(
      HistoryProvider.Context.Provider,
      { value: historyProvider },
      h(
        FSServices.Context.Provider,
        { value: fsServices },
        h(
          withPortalsComp,
          null,
          h(fileListComp, { dispatch, isRightInitiallyActive: false }),

          props.children,
        ),
      ),
    );
  };

  FileListRootComp.displayName = "FileListRoot";

  return FileListRootComp;
}

/** @type {readonly FileListPlugin[]} */
const plugins = [
  QuickViewPlugin,
  ArchiverPlugin.instance,
  ViewerPlugin,
  CopyMovePlugin.instance,
  FSPlugin.instance,
  FileListUiPlugin,
  FilePlugin,
];

FileListRoot.fileListComp = FileListBrowser(FileListPluginHandler(plugins));

export default FileListRoot;
