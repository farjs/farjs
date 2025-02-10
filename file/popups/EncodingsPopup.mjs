import React, { useLayoutEffect, useState } from "react";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import Encoding from "../Encoding.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly encoding: string;
 *  onApply(encoding: string): void;
 *  onClose(): void;
 * }} EncodingsPopupProps
 */

/**
 * @param {EncodingsPopupProps} props
 */
const EncodingsPopup = (props) => {
  const { listPopup } = EncodingsPopup;

  const [maybeItems, setItems] = useState(
    /** @type {string[] | undefined} */ (undefined)
  );

  useLayoutEffect(() => {
    setItems(Encoding.encodings);
  }, []);

  if (maybeItems !== undefined) {
    return h(listPopup, {
      title: "Encodings",
      items: maybeItems,
      onAction: (index) => {
        const enc = maybeItems[index];
        if (enc !== props.encoding) {
          props.onApply(enc);
        }
        props.onClose();
      },
      onClose: props.onClose,
      selected: Math.max(maybeItems.indexOf(props.encoding), 0),
    });
  }

  return null;
};

EncodingsPopup.displayName = "EncodingsPopup";
EncodingsPopup.listPopup = ListPopup;

export default EncodingsPopup;
