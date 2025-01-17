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
const ExitController = (props) => {
  const { messageBoxComp } = ExitController;

  const theme = Theme.useTheme().popup;

  return props.showExitPopup
    ? h(messageBoxComp, {
        title: "Exit",
        message: "Do you really want to exit FAR.js?",
        actions: [
          MessageBoxAction.YES(() => {
            props.onClose();
            process.stdin.emit("keypress", undefined, {
              name: "e",
              ctrl: true,
              meta: false,
              shift: false,
            });
          }),
          MessageBoxAction.NO(() => {
            props.onClose();
          }),
        ],
        style: theme.regular,
      })
    : null;
};

ExitController.displayName = "ExitController";
ExitController.messageBoxComp = MessageBox;

export default ExitController;
