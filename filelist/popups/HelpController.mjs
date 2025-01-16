/**
 * @typedef {import("../FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";

const h = React.createElement;

/**
 * @param {FileListUiData} props
 */
const HelpController = (props) => {
  const { messageBoxComp } = HelpController;

  const theme = Theme.useTheme().popup;

  return props.showHelpPopup
    ? h(messageBoxComp, {
        title: "Help",
        message: "//TODO: show help/about info",
        actions: [
          MessageBoxAction.OK(() => {
            props.onClose();
          }),
        ],
        style: theme.regular,
      })
    : null;
};

HelpController.displayName = "HelpController";
HelpController.messageBoxComp = MessageBox;

export default HelpController;
