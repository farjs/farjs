/**
 * @import { Dispatch, ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPluginLoader } from "@farjs/filelist/FileListPluginLoader.mjs"
 */
import React from "react";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import FSServices from "../../fs/FSServices.mjs";
import FSPluginLoader from "../../fs/FSPluginLoader.mjs";
import FilePluginLoader from "../../file/FilePluginLoader.mjs";
import FileListUiPluginLoader from "../../filelist/FileListUiPluginLoader.mjs";
import ArchiverPluginLoader from "../../archiver/ArchiverPluginLoader.mjs";
import CopyMovePluginLoader from "../../copymove/CopyMovePluginLoader.mjs";
import QuickViewPluginLoader from "../../viewer/quickview/QuickViewPluginLoader.mjs";
import ViewerPluginLoader from "../../viewer/ViewerPluginLoader.mjs";
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

/** @type {readonly FileListPluginLoader[]} */
const plugins = [
  QuickViewPluginLoader,
  ArchiverPluginLoader,
  ViewerPluginLoader,
  CopyMovePluginLoader,
  FSPluginLoader,
  FileListUiPluginLoader,
  FilePluginLoader,
];

FileListRoot.fileListComp = FileListBrowser(FileListPluginHandler(plugins));

export default FileListRoot;
