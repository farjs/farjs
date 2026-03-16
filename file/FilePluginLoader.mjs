import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["M-v"],
  lazyFn(() => {
    const module = "./FilePlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
