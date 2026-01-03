import os from "os";
import path from "path";
import { lazyFn } from "@farjs/filelist/utils.mjs";

const appName = "FAR.js";

/**
 * @typedef {{
 *  getDBFilePath(): string;
 *  getDataDir(): readonly string[];
 * }} FarjsData
 */

/**
 * @param {NodeJS.Platform} platform
 * @returns {readonly string[]}
 */
function getAppDataDir(platform) {
  const home = os.homedir();

  if (platform === "darwin") {
    return [home, "Library", "Application Support", appName];
  }

  if (platform === "win32") {
    const appDataDir = process.env.APPDATA;
    return appDataDir !== undefined
      ? [appDataDir, appName]
      : [home, `.${appName}`];
  }

  return [home, ".local", "share", appName];
}

/**
 * @param {NodeJS.Platform} platform
 * @returns {FarjsData}
 */
function FarjsData(platform) {
  const getDataDir = lazyFn(() => getAppDataDir(platform));

  return Object.freeze({
    getDBFilePath: lazyFn(() => path.join(...getDataDir(), "farjs.db")),

    getDataDir,
  });
}

FarjsData.instance = FarjsData(process.platform);

export default FarjsData;
