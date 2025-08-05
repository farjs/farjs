/**
 * @typedef {import("./FSDisk.mjs").FSDisk} FSDisk
 */
import nodeCp from "node:child_process";
import nodePath from "node:path";
import { stripPrefix, stripSuffix } from "@farjs/filelist/utils.mjs";
import FileListItem from "@farjs/filelist/api/FileListItem.mjs";
import FSDisk from "./FSDisk.mjs";

/**
 * @typedef {{
 *  exec(command: string, options: nodeCp.ExecOptions, callback?: (error: nodeCp.ExecException | null, stdout: string, stderr: string) => void): void;
 * }} NodeJSChildProcess
 */

/**
 * @typedef {{
 *  openItem(parent: string, item: string): Promise<void>;
 *  readDisk(path: string): Promise<FSDisk | undefined>;
 *  readDisks(): Promise<readonly FSDisk[]>;
 * }} FSService
 */

/**
 *
 * @param {NodeJS.Platform} platform
 * @param {NodeJSChildProcess} childProcess
 * @returns {FSService}
 */
function FSService(platform = process.platform, childProcess = nodeCp) {
  return {
    openItem: (parent, item) => {
      return new Promise((resolve, reject) => {
        const name =
          item === FileListItem.up.name ? FileListItem.currDir.name : item;
        const command =
          platform === "darwin"
            ? `open "${name}"`
            : platform === "win32"
            ? `start "" "${name}"`
            : `xdg-open "${name}"`;

        childProcess.exec(
          command,
          {
            cwd: parent,
            windowsHide: true,
          },
          (error) => {
            if (error) reject(error);
            else resolve();
          }
        );
      });
    },
    readDisk: (path) => {
      return new Promise((resolve, reject) => {
        const command = (() => {
          if (platform === "win32") {
            const root = stripSuffix(nodePath.parse(path).root, "\\");
            return `wmic logicaldisk where "Caption='${root}'" get Caption,VolumeName,FreeSpace,Size`;
          }

          return `df -kP "${path}"`;
        })();

        childProcess.exec(
          command,
          {
            cwd: path,
            windowsHide: true,
          },
          (error, output) => {
            if (error) reject(error);
            else {
              const disks =
                platform === "win32"
                  ? FSDisk.fromWmicLogicalDisk(output)
                  : FSDisk.fromDfCommand(output);

              disks.length > 0 ? resolve(disks[0]) : resolve(undefined);
            }
          }
        );
      });
    },
    readDisks: () => {
      return new Promise((resolve, reject) => {
        const command =
          platform === "win32"
            ? "wmic logicaldisk get Caption,VolumeName,FreeSpace,Size"
            : "df -kP";

        childProcess.exec(
          command,
          {
            windowsHide: true,
          },
          (error, output) => {
            if (error) reject(error);
            else {
              if (platform === "win32") {
                resolve(FSDisk.fromWmicLogicalDisk(output));
                return;
              }

              const disks = FSDisk.fromDfCommand(output);
              resolve(
                disks
                  .filter(
                    (d) => !excludeRoots.find((_) => d.name.startsWith(_))
                  )
                  .map((d) => {
                    return { ...d, name: stripPrefix(d.name, "/Volumes/") };
                  })
              );
            }
          }
        );
      });
    },
  };
}

/** @type {FSService} */
FSService.instance = FSService();

const excludeRoots = [
  "/dev",
  "/net",
  "/home",
  "/private/",
  "/System/",
  "/etc/",
  "/sys/",
];

export default FSService;
