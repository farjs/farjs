/**
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { Dispatch, ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { FSPluginUiOptions } from "./FSPluginUi.mjs"
 */
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListStateReducer from "@farjs/filelist/FileListStateReducer.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileListPanelController from "@farjs/filelist/FileListPanelController.mjs";
import FSFileListActions from "./FSFileListActions.mjs";
import FSPanel from "./FSPanel.mjs";
import FSPluginUi from "./FSPluginUi.mjs";

/**
 * @typedef {(s: FileListState, a: any) => FileListState} Reducer
 */

class FSPlugin extends FileListPlugin {
  /**
   * @param {Reducer} reducer
   */
  constructor(reducer) {
    super(["M-l", "M-r", "M-h", "C-d"]);

    /** @readonly @type {ReactComponent} */
    this.component = FileListPanelController(FSPanel);

    /** @private @readonly @type {Reducer} */
    this.reducer = reducer;
  }

  /**
   * @param {Dispatch} parentDispatch
   * @param {PanelStack} stack
   */
  init(parentDispatch, stack) {
    const self = this;
    stack.updateFor(this.component, (item) => {
      const newItem = self.initDispatch(
        parentDispatch,
        self.reducer,
        stack,
        item
      );
      return new PanelStackItem(
        newItem.component,
        newItem.dispatch,
        FSFileListActions.instance,
        FileListState()
      );
    });
  }

  /**
   * @param {Dispatch} parentDispatch
   * @param {Reducer} reducer
   * @param {PanelStack} stack
   * @param {PanelStackItem<FileListState>} item
   * @returns {PanelStackItem<FileListState>}
   */
  initDispatch(parentDispatch, reducer, stack, item) {
    /** @type {(action: any) => void} */
    const dispatch = (action) => {
      stack.updateFor(item.component, (item) => {
        return item.updateState((state) => {
          return reducer(state, action);
        });
      });
      parentDispatch(action);
    };

    return new PanelStackItem(
      item.component,
      dispatch,
      item.actions,
      item.state
    );
  }

  /** @type {FileListPlugin['onKeyTrigger']} */
  async onKeyTrigger(key) {
    const uiOptions = this._createUiOptions(key);
    if (!uiOptions) {
      return undefined;
    }

    return FSPluginUi(uiOptions);
  }

  /**
   * @param {string} key
   * @returns {FSPluginUiOptions | undefined}
   */
  _createUiOptions(key) {
    switch (key) {
      case "M-l":
        return {
          showDrivePopupOnLeft: true,
          showFoldersHistoryPopup: false,
          showFolderShortcutsPopup: false,
        };
      case "M-r":
        return {
          showDrivePopupOnLeft: false,
          showFoldersHistoryPopup: false,
          showFolderShortcutsPopup: false,
        };
      case "M-h":
        return {
          showDrivePopupOnLeft: undefined,
          showFoldersHistoryPopup: true,
          showFolderShortcutsPopup: false,
        };
      case "C-d":
        return {
          showDrivePopupOnLeft: undefined,
          showFoldersHistoryPopup: false,
          showFolderShortcutsPopup: true,
        };
      default:
        return undefined;
    }
  }
}

FSPlugin.instance = new FSPlugin(FileListStateReducer);

export default FSPlugin;
