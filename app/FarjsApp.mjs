/**
 * @typedef {import("@farjs/blessed").Widgets.Screen} BlessedScreen
 * @typedef {import("@farjs/blessed").Widgets.IScreenOptions} ScreenOptions
 * @typedef {import("./FarjsData.mjs").FarjsData} FarjsData
 * @import { Database } from "@farjs/better-sqlite3-wrapper"
 * @import { ReactComponent } from "@farjs/filelist/FileListData.mjs"
 * @import FileListActions from "@farjs/filelist/FileListActions.mjs"
 */
import React from "react";
import ReactBlessed from "react-blessed";
import Blessed from "@farjs/blessed";
import AppRoot from "@farjs/ui/app/AppRoot.mjs";
import DevTool from "@farjs/ui/tool/DevTool.mjs";
import WithPortals from "@farjs/ui/portal/WithPortals.mjs";
import FileListTheme from "@farjs/filelist/theme/FileListTheme.mjs";
import FileListModule from "./filelist/FileListModule.mjs";
import FileListRoot from "./filelist/FileListRoot.mjs";
import FSFileListActions from "../fs/FSFileListActions.mjs";
import FarjsData from "./FarjsData.mjs";
import FarjsDBMigrations from "./FarjsDBMigrations.mjs";

const h = React.createElement;

const FarjsApp = {
  /**
   * @param {boolean} showDevTools
   * @param {() => void} [onReady]
   * @param {() => void} [onExit]
   */
  start: (showDevTools, onReady, onExit) => {
    const screen = FarjsApp.createScreen(onExit);
    FarjsApp.render(showDevTools, screen, onReady);
  },

  /** @type {(onExit?: () => void) => BlessedScreen} */
  createScreen: (onExit) => {
    const screen = FarjsApp._blesseScreen({
      autoPadding: true,
      smartCSR: true,
      tabSize: 1,
      fullUnicode: true,
      cursorShape: "underline",
    });
    const savedConsoleLog = console.log;
    const savedConsoleError = console.error;

    screen.key(["C-e"], () => {
      // cleanup/unmount components
      screen.destroy();

      console.log = savedConsoleLog;
      console.error = savedConsoleError;

      if (onExit) onExit();
      else process.exit(0);
    });

    return screen;
  },

  /** @type {(showDevTools: boolean, screen: BlessedScreen, onReady?: () => void) => void} */
  render: (showDevTools, screen, onReady) => {
    FarjsApp._renderer(
      h(FarjsApp.appRootComp, {
        loadMainUi: async (dispatch) => {
          if (onReady) {
            onReady();
          }

          const db = await FarjsApp._prepareDB(
            FSFileListActions.instance,
            FarjsData.instance,
          );
          const fileListModule = new FileListModule(db);
          const mainUi = FileListRoot(
            dispatch,
            fileListModule,
            FarjsApp._createPortals(screen),
          );
          const theme =
            screen.terminal === "xterm-256color"
              ? FileListTheme.xterm256Theme
              : FileListTheme.defaultTheme;

          return { theme, mainUi };
        },
        initialDevTool: showDevTools ? DevTool.Logs : DevTool.Hidden,
        defaultTheme: FileListTheme.defaultTheme,
      }),
      screen,
    );
  },

  /** @type {(actions: FileListActions, appData: FarjsData) => Promise<Database>} */
  _prepareDB: FarjsDBMigrations.prepareDB,

  /** @type {(s: BlessedScreen) => ReactComponent} */
  _createPortals: WithPortals.create,

  /** @type {(options: ScreenOptions) => BlessedScreen} */
  _blesseScreen: Blessed.screen,

  /** @type {(c: React.ReactElement, s: BlessedScreen) => void} */
  _renderer: ReactBlessed.createBlessedRenderer(Blessed),

  appRootComp: AppRoot,
};

export default FarjsApp;
