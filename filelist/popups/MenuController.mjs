/**
 * @typedef {import("@farjs/ui/menu/MenuBar.mjs").MenuBarItem} MenuBarItem
 * @typedef {import("../FileListUi.mjs").FileListUiData} FileListUiData
 */
import React from "react";
import MenuBar from "@farjs/ui/menu/MenuBar.mjs";
import SubMenu from "@farjs/ui/menu/SubMenu.mjs";
import { stripPrefix } from "@farjs/filelist/utils.mjs";
import WithStacks from "@farjs/filelist/stack/WithStacks.mjs";

const h = React.createElement;

/**
 * @param {FileListUiData} props
 */
const MenuController = (props) => {
  const { menuBarComp } = MenuController;

  const stacks = WithStacks.useStacks();

  /** @type {(menuIndex: number, subIndex: number) => void} */
  const onAction = (menuIndex, subIndex) => {
    props.onClose();

    const [_, subMenu] = actions[menuIndex];
    const { isRight, keyFull } = subMenu[subIndex];
    const data = {
      name: stripPrefix(stripPrefix(stripPrefix(keyFull, "C-"), "M-"), "S-"),
      full: keyFull,
      ctrl: keyFull.startsWith("C-"),
      meta: keyFull.startsWith("M-"),
      shift: keyFull.startsWith("S-"),
    };
    switch (isRight) {
      case undefined:
        process.stdin.emit("keypress", undefined, data);
        break;
      case false:
        stacks.left.input.emit("keypress", undefined, data);
        break;
      case true:
        stacks.right.input.emit("keypress", undefined, data);
        break;
    }
  };

  return props.showMenuPopup
    ? h(menuBarComp, {
        items: MenuController._items,
        onAction,
        onClose: props.onClose,
      })
    : null;
};

MenuController.displayName = "MenuController";
MenuController.menuBarComp = MenuBar;

/**
 * @typedef {{
 *  readonly item: string;
 *  readonly isRight?: boolean;
 *  readonly keyFull: string;
 * }} SubMenuItem
 */

/**
 *
 * @param {string} item
 * @param {boolean | undefined} isRight
 * @param {string} keyFull
 * @returns {SubMenuItem}
 */
function subMenuItem(item, isRight, keyFull) {
  return { item, isRight, keyFull };
}

/** @type {readonly [string, readonly SubMenuItem[]][]} */
const actions = [
  [
    "Left",
    [
      subMenuItem("  Quick view    Ctrl-Q    ", true, "C-q"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Sort modes    Ctrl-F12  ", false, "C-f12"),
      subMenuItem("  Re-read       Ctrl-R    ", false, "C-r"),
      subMenuItem("  Change drive  Alt-L     ", false, "M-l"),
    ],
  ],
  [
    "Files",
    [
      subMenuItem("  View            F3        ", undefined, "f3"),
      subMenuItem("  Copy            F5        ", undefined, "f5"),
      subMenuItem("  Rename or move  F6        ", undefined, "f6"),
      subMenuItem("  Make folder     F7        ", undefined, "f7"),
      subMenuItem("  Delete          F8        ", undefined, "f8"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Add to archive  Shift-F7  ", undefined, "S-f7"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Select group    Alt-S     ", undefined, "M-s"),
      subMenuItem("  Unselect group  Alt-D     ", undefined, "M-d"),
    ],
  ],
  [
    "Commands",
    [
      subMenuItem("  File view history  Alt-V   ", undefined, "M-v"),
      subMenuItem("  Folders history    Alt-H   ", undefined, "M-h"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Swap panels        Ctrl-U  ", undefined, "C-u"),
      subMenuItem("  Quick search       Ctrl-S  ", undefined, "C-s"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Folder shortcuts   Ctrl-D  ", undefined, "C-d"),
    ],
  ],
  ["Options", [subMenuItem("  DevTools    F12  ", undefined, "f12")]],
  [
    "Right",
    [
      subMenuItem("  Quick view    Ctrl-Q    ", false, "C-q"),
      subMenuItem(SubMenu.separator, undefined, ""),
      subMenuItem("  Sort modes    Ctrl-F12  ", true, "C-f12"),
      subMenuItem("  Re-read       Ctrl-R    ", true, "C-r"),
      subMenuItem("  Change drive  Alt-R     ", true, "M-r"),
    ],
  ],
];

/** @type {readonly MenuBarItem[]} */
MenuController._items = actions.map(([name, subItems]) => {
  return { label: name, subItems: subItems.map(({ item }) => item) };
});

export default MenuController;
