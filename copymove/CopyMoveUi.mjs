/**
 * @import { FileListItem } from "@farjs/filelist/api/FileListItem.mjs"
 * @import { FileListAction } from "@farjs/filelist/FileListActions.mjs"
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { FileListPluginUiProps } from "@farjs/filelist/FileListPlugin.mjs"
 */
import path from "path";
import React, { useRef, useState } from "react";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Task from "@farjs/ui/task/Task.mjs";
import TaskAction from "@farjs/ui/task/TaskAction.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import { isEqualSets, stripPrefix } from "@farjs/filelist/utils.mjs";
import CopyItemsPopup from "./CopyItemsPopup.mjs";
import CopyItemsStats from "./CopyItemsStats.mjs";
import CopyProcess from "./CopyProcess.mjs";
import MoveProcess from "./MoveProcess.mjs";

const h = React.createElement;

/**
 * @typedef {"ShowCopyToTarget"
 *  | "ShowCopyInplace"
 *  | "ShowMoveToTarget"
 *  | "ShowMoveInplace"
 * } CopyMoveUiAction
 */

/**
 * @typedef {{
 *  readonly show: CopyMoveUiAction;
 *  readonly from: FileListData;
 *  readonly maybeTo?: FileListData;
 * }} CopyMoveUiOptions
 */

/**
 * @param {CopyMoveUiOptions} options
 */
function CopyMoveUi({ show, from, maybeTo }) {
  /**
   * @param {FileListPluginUiProps} props
   */
  const CopyMoveUiComp = (props) => {
    const {
      copyItemsPopup,
      copyItemsStats,
      messageBoxComp,
      moveProcessComp,
      copyProcessComp,
    } = CopyMoveUi;

    const historyProvider = HistoryProvider.useHistoryProvider();
    const [maybeTotal, setTotal] = useState(
      /** @type {number | undefined} */ (undefined),
    );
    const [maybeToPath, setToPath] = useState(
      /** @type {[string, string] | undefined} */ (undefined),
    );
    const [inplace, setInplace] = useState(false);
    const [move, setMove] = useState(false);
    const [showPopup, setShowPopup] = useState(true);
    const [showStats, setShowStats] = useState(false);
    const [showMove, setShowMove] = useState(false);
    /** @type {React.MutableRefObject<Set<string>>} */
    const copied = useRef(new Set([]));
    const currTheme = Theme.useTheme();

    /** @type {(item: FileListItem) => any} */
    const onTopItem = (item) => copied.current.add(item.name);

    /** @type {(path: string, toPath: string) => () => void} */
    const onDone = (path, toPath) => () => {
      const currSelected = from.state.selectedNames;
      const newSelected = new Set([...currSelected]);
      copied.current.forEach((_) => newSelected.delete(_));
      if (!isEqualSets(currSelected, newSelected)) {
        /** @type {FileListAction} */
        const action = {
          action: "FileListParamsChangedAction",
          offset: from.state.offset,
          index: from.state.index,
          selectedNames: newSelected,
        };
        from.dispatch(action);
      }

      const isInplace = inplace;
      props.onClose();

      historyProvider
        .get(CopyItemsPopup.copyItemsHistoryKind)
        .then((copyItemsHistory) => copyItemsHistory.save({ item: path }));

      const updateAction = from.actions.updateDir(
        from.dispatch,
        from.state.currDir.path,
      );
      from.dispatch(updateAction);
      updateAction.task.result.then((updatedDir) => {
        if (isInplace) {
          /** @type {FileListAction} */
          const action = {
            action: "FileListItemCreatedAction",
            name: toPath,
            currDir: updatedDir,
          };
          from.dispatch(action);
        } else if (maybeTo !== undefined) {
          maybeTo.dispatch(
            maybeTo.actions.updateDir(
              maybeTo.dispatch,
              maybeTo.state.currDir.path,
            ),
          );
        }
      });
    };

    /** @type {() => boolean} */
    const isMove = () =>
      move || show === "ShowMoveToTarget" || show === "ShowMoveInplace";

    /** @type {() => boolean} */
    const isInplace = () =>
      inplace ||
      show === "ShowCopyInplace" ||
      show === "ShowMoveInplace" ||
      ((maybeTo === undefined ||
        maybeTo.state.currDir.path === from.state.currDir.path) &&
        FileListState.selectedItems(from.state).length === 0);

    const fromSelected = FileListState.selectedItems(from.state);
    const items =
      !isInplace() && fromSelected.length > 0
        ? fromSelected
        : [FileListState.currentItem(from.state)].filter(
            (_) => _ !== undefined,
          );

    /**
     * @param {boolean} move
     * @param {string} path
     * @returns {Promise<[string, boolean]>}
     */
    function resolveTargetDir(move, path) {
      /** @type {Promise<[string, boolean]>} */
      const dirP = (async () => {
        const dir = await from.actions.api.readDir(
          from.state.currDir.path,
          path,
        );
        const sameDrive = move ? await checkSameDrive(from, dir.path) : false;
        return [dir.path, sameDrive];
      })();

      from.dispatch(TaskAction(Task("Resolving target dir", dirP)));
      return dirP;
    }

    /** @type {(path: string) => void} */
    function onAction(path) {
      const move = isMove();
      const inplace = isInplace();

      /** @type {Promise<[string, boolean]>} */
      const resolveP = !from.actions.api.isLocal
        ? Promise.resolve([path, false])
        : !inplace
          ? resolveTargetDir(move, path)
          : Promise.resolve([path, true]);

      resolveP.then(([toPath, sameDrive]) => {
        setInplace(inplace);
        setMove(move);
        setShowPopup(false);

        setToPath([path, toPath]);
        if (move && sameDrive) setShowMove(true);
        else setShowStats(true);
      });
    }

    const maybeError = (() => {
      if (!maybeToPath || inplace) {
        return undefined;
      }
      const [_, toPath] = maybeToPath;
      const op = move ? "move" : "copy";
      if (from.state.currDir.path === toPath) {
        return `Cannot ${op} the item\n${items[0].name}\nonto itself`;
      }
      if (toPath.startsWith(from.state.currDir.path + path.sep)) {
        const toSuffix = stripPrefix(
          toPath,
          from.state.currDir.path + path.sep,
        );
        const self = items.find(
          (i) => toSuffix === i.name || toSuffix.startsWith(i.name + path.sep),
        );
        return self && `Cannot ${op} the item\n${self.name}\ninto itself`;
      }
      return undefined;
    })();

    return showPopup
      ? h(copyItemsPopup, {
          move: isMove(),
          path: (() => {
            const maybePath =
              !isInplace() && maybeTo
                ? [maybeTo.state.currDir.path]
                : [FileListState.currentItem(from.state)]
                    .filter((_) => _ !== undefined)
                    .map((_) => _.name);
            return maybePath.length > 0 ? maybePath[0] : "";
          })(),
          items,
          onAction,
          onCancel: props.onClose,
        })
      : maybeError !== undefined
        ? h(messageBoxComp, {
            title: "Error",
            message: maybeError,
            actions: [MessageBoxAction.OK(props.onClose)],
            style: currTheme.popup.error,
          })
        : showStats
          ? h(copyItemsStats, {
              dispatch: from.dispatch,
              actions: from.actions,
              fromPath: from.state.currDir.path,
              items,
              title: move ? "Move" : "Copy",
              onDone: (total) => {
                setTotal(total);
                setShowStats(false);
              },
              onCancel: props.onClose,
            })
          : showMove && maybeToPath !== undefined
            ? (([path, toPath]) => {
                return h(moveProcessComp, {
                  dispatch: from.dispatch,
                  actions: from.actions,
                  fromPath: from.state.currDir.path,
                  items: !inplace
                    ? items.map((item) => ({ item, toName: item.name }))
                    : items.map((item) => ({ item, toName: toPath })),
                  toPath: !inplace ? toPath : from.state.currDir.path,
                  onTopItem,
                  onDone: onDone(path, toPath),
                });
              })(maybeToPath)
            : maybeTotal !== undefined && maybeToPath !== undefined
              ? ((total, [path, toPath]) => {
                  return h(copyProcessComp, {
                    from,
                    to: !inplace && maybeTo ? maybeTo : from,
                    move,
                    fromPath: from.state.currDir.path,
                    items: !inplace
                      ? items.map((item) => ({ item, toName: item.name }))
                      : items.map((item) => ({ item, toName: toPath })),
                    toPath: !inplace ? toPath : from.state.currDir.path,
                    total,
                    onTopItem,
                    onDone: onDone(path, toPath),
                  });
                })(maybeTotal, maybeToPath)
              : null;
  };

  CopyMoveUiComp.displayName = "CopyMoveUi";

  return CopyMoveUiComp;
}

CopyMoveUi.copyItemsPopup = CopyItemsPopup;
CopyMoveUi.copyItemsStats = CopyItemsStats;
CopyMoveUi.messageBoxComp = MessageBox;
CopyMoveUi.moveProcessComp = MoveProcess;
CopyMoveUi.copyProcessComp = CopyProcess;

/**
 * @param {FileListData} from
 * @param {string} toPath
 * @returns {Promise<boolean>}
 */
async function checkSameDrive(from, toPath) {
  const maybeFromRoot = await from.actions.api.getDriveRoot(
    from.state.currDir.path,
  );
  const maybeToRoot = await from.actions.api.getDriveRoot(toPath);
  return (
    maybeFromRoot !== undefined &&
    maybeToRoot !== undefined &&
    maybeFromRoot === maybeToRoot
  );
}

export default CopyMoveUi;
