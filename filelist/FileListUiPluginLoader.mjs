import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"],
  lazyFn(() => {
    const module = "./FileListUiPlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
