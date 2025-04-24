/**
 * @typedef {import("../../fs/popups/FolderShortcutsService.mjs").FolderShortcutsService} FolderShortcutsService
 */
import React from "react";
import FSServices from "../../fs/FSServices.mjs";

const h = React.createElement;

/**
 * @param {React.ReactElement} element
 * @param {FolderShortcutsService} folderShortcuts
 * @returns {React.ReactElement}
 */
const withServicesContext = (element, folderShortcuts) => {
  return h(
    FSServices.Context.Provider,
    { value: { folderShortcuts } },
    element
  );
};

export default withServicesContext;
