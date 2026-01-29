/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @import { FileListData } from "@farjs/filelist/FileListData.mjs"
 * @import { CopyMoveUiAction } from "./CopyMoveUi.mjs"
 */
import FileListCapability from "@farjs/filelist/api/FileListCapability.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import PanelStack from "@farjs/filelist/stack/PanelStack.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import FileListEvent from "@farjs/filelist/FileListEvent.mjs";
import CopyMoveUi from "./CopyMoveUi.mjs";

class CopyMovePlugin extends FileListPlugin {
  constructor() {
    super(["f5", "f6", "S-f5", "S-f6"]);
  }

  /** @type {FileListPlugin['onKeyTrigger']} */
  async onKeyTrigger(key, stacks) {
    /** @type {(stack: PanelStack) => FileListData | undefined } */
    const getData = (stack) => stack.peek().getData();

    const [from, maybeTo, toInput] = stacks.left.stack.isActive
      ? [
          getData(stacks.left.stack),
          getData(stacks.right.stack),
          stacks.right.input,
        ]
      : [
          getData(stacks.right.stack),
          getData(stacks.left.stack),
          stacks.left.input,
        ];

    switch (key) {
      case "f5":
      case "f6":
        if (from && maybeTo) {
          const action = this._onCopyMove(key === "f6", from, maybeTo, toInput);
          return action
            ? CopyMoveUi({ show: action, from, maybeTo })
            : undefined;
        }
        return undefined;
      case "S-f5":
      case "S-f6":
        if (from) {
          const action = this._onCopyMoveInplace(key === "S-f6", from);
          return action ? CopyMoveUi({ show: action, from }) : undefined;
        }
        return undefined;
    }

    return undefined;
  }

  /**
   * @param {boolean} move
   * @param {FileListData} from
   * @returns {CopyMoveUiAction | undefined}
   */
  _onCopyMoveInplace(move, from) {
    if (FileListState.currentItem(from.state, (i) => i !== FileListItem.up)) {
      if (
        move &&
        from.actions.api.capabilities.has(FileListCapability.moveInplace)
      ) {
        return "ShowMoveInplace";
      }
      if (
        !move &&
        from.actions.api.capabilities.has(FileListCapability.copyInplace)
      ) {
        return "ShowCopyInplace";
      }
    }

    return undefined;
  }

  /**
   * @param {boolean} move
   * @param {FileListData} from
   * @param {FileListData} to
   * @param {BlessedElement} toInput
   * @returns {CopyMoveUiAction | undefined}
   */
  _onCopyMove(move, from, to, toInput) {
    const currItem = FileListState.currentItem(
      from.state,
      (i) => i !== FileListItem.up,
    );

    if (
      (from.state.selectedNames.size > 0 || currItem) &&
      from.actions.api.capabilities.has(FileListCapability.read) &&
      (!move || from.actions.api.capabilities.has(FileListCapability.delete))
    ) {
      if (to.actions.api.capabilities.has(FileListCapability.write)) {
        return move ? "ShowMoveToTarget" : "ShowCopyToTarget";
      }

      toInput.emit("keypress", undefined, {
        name: "",
        full: move
          ? FileListEvent.onFileListMove
          : FileListEvent.onFileListCopy,
      });
    }

    return undefined;
  }
}

CopyMovePlugin.instance = new CopyMovePlugin();

export default CopyMovePlugin;
