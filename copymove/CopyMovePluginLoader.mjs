import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["f5", "f6", "S-f5", "S-f6"],
  lazyFn(() => {
    const module = "./CopyMovePlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
