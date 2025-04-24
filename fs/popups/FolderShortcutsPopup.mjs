/**
 * @import PanelStackItem from "@farjs/filelist/stack/PanelStackItem.mjs"
 * @import { FileListState } from "@farjs/filelist/FileListState.mjs"
 */
import React, { useLayoutEffect, useState } from "react";
import ListPopup from "@farjs/ui/popup/ListPopup.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";
import FSServices from "../FSServices.mjs";
import WithStacksProps from "@farjs/filelist/stack/WithStacksProps.mjs";

const h = React.createElement;

/**
 * @typedef {{
 *  onChangeDir(dir: string): void;
 *  onClose(): void;
 * }} FolderShortcutsPopupProps
 */

/**
 * @param {FolderShortcutsPopupProps} props
 */
const FolderShortcutsPopup = (props) => {
  const { listPopup } = FolderShortcutsPopup;

  const stacks = WithStacks.useStacks();
  const services = FSServices.useServices();
  const [maybeItems, setItems] = useState(
    /** @type {readonly (string | undefined)[] | undefined} */ (undefined)
  );
  const [selected, setSelected] = useState(0);

  /** @type {(index: number) => void} */
  function onAction(index) {
    if (maybeItems !== undefined) {
      const dir = maybeItems[index];
      if (dir !== undefined) {
        props.onChangeDir(dir);
      }
    }
  }

  /** @type {(key: string) => boolean} */
  function onKeypress(key) {
    let processed = true;
    switch (key) {
      case "0":
      case "1":
      case "2":
      case "3":
      case "4":
      case "5":
      case "6":
      case "7":
      case "8":
      case "9":
        onAction(parseInt(key));
        break;
      case "-":
        services.folderShortcuts.delete(selected).then(() => {
          if (maybeItems !== undefined) {
            const newItems = [...maybeItems];
            newItems[selected] = undefined;
            setItems(newItems);
          }
        });
        break;
      case "+":
        /** @type {PanelStackItem<FileListState>} */
        const stackItem = WithStacksProps.active(stacks).stack.peekLast();
        const state = stackItem.state;
        if (state) {
          const dir = state.currDir.path;
          services.folderShortcuts.save(selected, dir).then(() => {
            if (maybeItems !== undefined) {
              const newItems = [...maybeItems];
              newItems[selected] = dir;
              setItems(newItems);
            }
          });
        }
        break;
      default:
        processed = false;
        break;
    }
    return processed;
  }

  useLayoutEffect(() => {
    services.folderShortcuts.getAll().then((shortcuts) => {
      setItems(shortcuts);
    });
  }, []);

  return maybeItems !== undefined
    ? h(listPopup, {
        title: "Folder shortcuts",
        items: maybeItems.map(
          (maybeItem, i) => `${i}: ${maybeItem ?? "<none>"}`
        ),
        onAction: onAction,
        onClose: props.onClose,
        onSelect: setSelected,
        onKeypress: onKeypress,
        footer: "Edit: +, -",
      })
    : null;
};

FolderShortcutsPopup.displayName = "FolderShortcutsPopup";
FolderShortcutsPopup.listPopup = ListPopup;

export default FolderShortcutsPopup;
