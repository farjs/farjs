/**
 * @import { Dispatch } from "@farjs/filelist/FileListData.mjs"
 */
import React from "react";
import DrivePopup from "./DrivePopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly dispatch: Dispatch;
 *  readonly showDrivePopupOnLeft?: boolean;
 *  onChangeDir(dir: string, showOnLeft: boolean): void;
 *  onClose(): void;
 * }} DriveControllerProps
 */

/**
 * @param {DriveControllerProps} props
 */
const DriveController = (props) => {
  const { drivePopup } = DriveController;

  const showOnLeft = props.showDrivePopupOnLeft;

  return showOnLeft !== undefined
    ? h(drivePopup, {
        dispatch: props.dispatch,
        onChangeDir: (dir) => {
          props.onClose();
          props.onChangeDir(dir, showOnLeft);
        },
        onClose: props.onClose,
        showOnLeft: showOnLeft,
      })
    : null;
};

DriveController.displayName = "DriveController";
DriveController.drivePopup = DrivePopup;

export default DriveController;
