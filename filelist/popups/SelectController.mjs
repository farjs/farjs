/**
 * @typedef {import("@farjs/filelist/FileListActions.mjs").FileListAction} FileListAction
 * @typedef {import("../FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import { isEqualSets } from "@farjs/filelist/utils.mjs";
import HistoryProvider from "@farjs/filelist/history/HistoryProvider.mjs";
import SelectPopup from "./SelectPopup.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";

const h = React.createElement;

/**
 * @param {FileListUiData} props
 */
const SelectController = (props) => {
  const { selectPopupComp } = SelectController;

  const historyProvider = HistoryProvider.useHistoryProvider();
  const data = props.data;
  const showSelectPopup = props.showSelectPopup;

  if (data && showSelectPopup !== undefined) {
    /** @type {(pattern: string) => Promise<void>} */
    const saveHistory = async (pattern) => {
      const selectPatternsHistory = await historyProvider.get(
        SelectPopup.selectPatternsHistoryKind
      );
      await selectPatternsHistory.save({ item: pattern });
    };

    return h(selectPopupComp, {
      showSelect: showSelectPopup,
      onAction: (pattern) => {
        saveHistory(pattern);

        const regexes = pattern
          .split(";")
          .map((mask) => new RegExp(SelectController._fileMaskToRegex(mask)));
        const matchedItems = data.state.currDir.items.filter(
          (i) => i !== FileListItem.up && regexes.find((_) => _.test(i.name))
        );

        const currSelected = data.state.selectedNames;
        const newSelected = new Set([...currSelected]);
        if (showSelectPopup) {
          matchedItems.forEach((_) => newSelected.add(_.name));
        } else {
          matchedItems.forEach((_) => newSelected.delete(_.name));
        }

        if (!isEqualSets(currSelected, newSelected)) {
          /** @type {FileListAction} */
          const action = {
            action: "FileListParamsChangedAction",
            offset: data.state.offset,
            index: data.state.index,
            selectedNames: newSelected,
          };
          data.dispatch(action);
        }

        props.onClose();
      },
      onCancel: props.onClose,
    });
  }

  return null;
};

SelectController.displayName = "SelectController";
SelectController.selectPopupComp = SelectPopup;

// consider supporting full glob pattern:
//  https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns/17369948#17369948
//
/** @type {(mask: string) => string} */
SelectController._fileMaskToRegex = (mask) => {
  return (
    "^" +
    escapeSpecials(mask).replaceAll("\\*", ".*?").replaceAll("\\?", ".") +
    "$"
  );
};

/** @type {RegExp} */
const escapeRegex = /[.*+\-?^${}()|\[\]\\]/;

// got from:
//   https://stackoverflow.com/questions/3561493/is-there-a-regexp-escape-function-in-javascript/63838890#63838890
//
/** @type {(regex: string) => string} */
function escapeSpecials(mask) {
  return mask.replace(new RegExp(escapeRegex, "g"), "\\$&"); // $& means the whole matched string
}

export default SelectController;
