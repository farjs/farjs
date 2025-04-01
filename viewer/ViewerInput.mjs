/**
 * @typedef {import("@farjs/blessed").Widgets.BlessedElement} BlessedElement
 * @typedef {import("@farjs/blessed").Widgets.Events.IKeyEventArg} IKeyEventArg
 */
import React, { useLayoutEffect, useRef } from "react";

const h = React.createElement;

/**
 * @typedef {{
 *  readonly inputRef: React.MutableRefObject<BlessedElement | null>;
 *  onWheel(up: boolean): void;
 *  onKeypress(keyFull: string): void;
 * }} ViewerInputProps
 */

/**
 * @param {React.PropsWithChildren<ViewerInputProps>} props
 * @returns {React.ReactElement | null}
 */
const ViewerInput = (props) => {
  const propsRef = useRef(props);
  propsRef.current = props;
  const inputEl = props.inputRef.current;

  useLayoutEffect(() => {
    if (inputEl) {
      /** @type {(ch: any, key: IKeyEventArg) => void} */
      const keyListener = (_, key) => {
        propsRef.current.onKeypress(key.full);
      };
      const wheelupListener = () => {
        propsRef.current.onWheel(true);
      };
      const wheeldownListener = () => {
        propsRef.current.onWheel(false);
      };

      inputEl.on("keypress", keyListener);
      inputEl.on("wheelup", wheelupListener);
      inputEl.on("wheeldown", wheeldownListener);

      return () => {
        inputEl.off("keypress", keyListener);
        inputEl.off("wheelup", wheelupListener);
        inputEl.off("wheeldown", wheeldownListener);
      };
    }

    return undefined;
  }, [inputEl]);

  return props.children ? h(React.Fragment, null, props.children) : null;
};

ViewerInput.displayName = "ViewerInput";

export default ViewerInput;
