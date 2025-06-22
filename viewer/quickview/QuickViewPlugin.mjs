/**
 * @typedef {import("@farjs/filelist/stack/WithStacksProps.mjs").WithStacksProps} WithStacksProps
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import { QuickViewParams } from "./QuickViewDir.mjs";
 */
import FileListPlugin from "@farjs/filelist/FileListPlugin.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";
import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs";
import QuickViewPanel from "./QuickViewPanel.mjs";

class QuickViewPluginImpl extends FileListPlugin {
  constructor() {
    super(["C-q"]);

    /** @type {ReactComponent} */
    this.panelComp = QuickViewPanel;
  }

  /**
   * @param {string} _
   * @param {WithStacksProps} stacks
   * @returns {Promise<ReactComponent | undefined>}
   */
  onKeyTrigger(_, stacks) {
    const exists = (() => {
      if (stacks.left.stack.peek().component === this.panelComp) {
        stacks.left.stack.pop();
        return true;
      }
      if (stacks.right.stack.peek().component === this.panelComp) {
        stacks.right.stack.pop();
        return true;
      }

      return false;
    })();

    if (!exists) {
      /** @type {QuickViewParams} */
      const params = {
        name: "",
        parent: "",
        folders: 0,
        files: 0,
        filesSize: 0,
      };

      const stack = WithStacksProps.nonActive(stacks).stack;
      stack.push(
        new PanelStackItem(this.panelComp, undefined, undefined, params)
      );
    }

    return Promise.resolve(undefined);
  }
}

const QuickViewPlugin = new QuickViewPluginImpl();

export default QuickViewPlugin;
