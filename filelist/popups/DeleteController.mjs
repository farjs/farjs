/**
 * @typedef {import("../FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import MessageBox from "@farjs/ui/popup/MessageBox.mjs";
import MessageBoxAction from "@farjs/ui/popup/MessageBoxAction.mjs";
import Theme from "@farjs/ui/theme/Theme.mjs";
import FileListState from "@farjs/filelist/FileListState.mjs";

const h = React.createElement;

/**
 * @param {FileListUiData} props
 */
const DeleteController = (props) => {
  const { messageBoxComp } = DeleteController;

  const theme = Theme.useTheme().popup;
  const data = props.data;

  if (data && props.showDeletePopup) {
    const onAction = () => {
      const currItem = FileListState.currentItem(data.state);
      const items = FileListState.selectedItems(data.state);
      if (items.length === 0 && currItem) {
        items.push(currItem);
      }

      props.onClose();
      data.dispatch(
        data.actions.deleteItems(data.dispatch, data.state.currDir.path, items)
      );
    };

    return h(messageBoxComp, {
      title: "Delete",
      message: "Do you really want to delete selected item(s)?",
      actions: [
        MessageBoxAction.YES(onAction),
        MessageBoxAction.NO(() => {
          props.onClose();
        }),
      ],
      style: theme.error,
    });
  }

  return null;
};

DeleteController.displayName = "DeleteController";
DeleteController.messageBoxComp = MessageBox;

export default DeleteController;
