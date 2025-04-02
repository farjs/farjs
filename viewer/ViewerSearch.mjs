import React, { useLayoutEffect } from "react";
import StatusPopup from "@farjs/ui/popup/StatusPopup.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly searchTerm: string;
 *  onComplete(): void;
 * }} ViewerSearchProps
 */

/**
 * @param {ViewerSearchProps} props
 */
const ViewerSearch = (props) => {
  const { statusPopupComp } = ViewerSearch;

  useLayoutEffect(() => {
    //TODO: start search
  }, []);

  return h(statusPopupComp, {
    text: `Searching for\n"${props.searchTerm}"`,
    title: "Search",
    onClose: () => {
      // stop search
      props.onComplete();
      //inProgress.current = false
    },
  });
};

ViewerSearch.displayName = "ViewerSearch";
ViewerSearch.statusPopupComp = StatusPopup;

export default ViewerSearch;
