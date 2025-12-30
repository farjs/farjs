/**
 * @typedef {import("@farjs/blessed").Widgets.Events.IKeyEventArg & {
 *    data?: any
 * }} IKeyEventArg
 * @typedef {import("@farjs/filelist/FileListState.mjs").FileListState} FileListState
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import PanelStack from "@farjs/filelist/stack/PanelStack.mjs"
 * @import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs"
 * @import { WithStacksProps } from "@farjs/filelist/stack/WithStacksProps.mjs"
 * @import { Dispatch, ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs"
 */
import path from "path";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListStateReducer from "@farjs/filelist/FileListStateReducer.mjs";
import FSPlugin from "../../fs/FSPlugin.mjs";

/**
 * @typedef {{
 *  openCurrItem(dispatch: Dispatch, stack: PanelStack): void;
 *  openPluginUi(dispatch: Dispatch, key: IKeyEventArg, stacks: WithStacksProps): Promise<ReactComponent | undefined>;
 * }} FileListPluginHandler
 */

/**
 * @param {readonly FileListPlugin[]} plugins
 * @returns {FileListPluginHandler}
 */
function FileListPluginHandler(plugins) {
  return {
    openCurrItem: (dispatch, stack) => {
      const stackItem = stack.peek();
      const data = stackItem.getData();
      if (
        data &&
        data.actions.api.isLocal &&
        FileListState.currentItem(data.state, (_) => !_.isDir)
      ) {
        const { actions, state } = data;
        const item = FileListState.currentItem(state);
        if (item !== undefined) {
          const filePath = path.join(state.currDir.path, item.name);

          /** @type {(item: FileListItem) => Promise<void>} */
          async function doOpen(item) {
            const source = await actions.api.readFile(
              state.currDir.path,
              item,
              0
            );
            const buff = new Uint8Array(64 * 1024);
            const bytesRead = await source.readNextBytes(buff);
            await source.close();
            const fileHeader = buff.subarray(0, bytesRead);

            /** @type {Promise<PanelStackItem<FileListState> | undefined>} */
            const zero = Promise.resolve(undefined);
            const pluginRes = plugins.reduce(async (resP, plugin) => {
              const res = await resP;
              return res === undefined
                ? plugin.onFileTrigger(filePath, fileHeader, () => stack.pop())
                : resP;
            }, zero);

            const pluginItem = await pluginRes;
            if (pluginItem !== undefined) {
              stack.push(
                FSPlugin.instance.initDispatch(
                  dispatch,
                  FileListStateReducer,
                  stack,
                  pluginItem
                )
              );
            }
          }

          const openP = doOpen(item);
          openP.catch(() => {
            dispatch(TaskAction(Task("Opening File Plugin", openP)));
          });
        }
      }
    },

    openPluginUi: (dispatch, key, stacks) => {
      const plugin = plugins.find((_) => _.triggerKeys.includes(key.full));
      if (!plugin) {
        return Promise.resolve(undefined);
      }

      const pluginRes = plugin.onKeyTrigger(key.full, stacks, key.data);
      return pluginRes.catch(() => {
        dispatch(TaskAction(Task("Opening Plugin Ui", pluginRes)));
        return undefined;
      });
    },
  };
}

export default FileListPluginHandler;
