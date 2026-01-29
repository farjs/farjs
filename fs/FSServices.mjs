/**
 * @import { FolderShortcutsService } from "./popups/FolderShortcutsService.mjs"
 */
import React, { useContext } from "react";

const FSServices = {
  Context: React.createContext(/** @type {FSServices | null} */ (null)),

  /**
   * @returns {FSServices}
   */
  useServices: () => {
    const ctx = useContext(FSServices.Context);
    if (!ctx) {
      throw Error(
        "FSServices.Context is not found." +
          "\nPlease, make sure you use FSServices.Context.Provider in parent components",
      );
    }
    return ctx;
  },
};

export default FSServices;

/**
 * @typedef {{
 *  readonly folderShortcuts: FolderShortcutsService;
 * }} FSServices
 */
