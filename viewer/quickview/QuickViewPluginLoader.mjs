import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["C-q"],
  lazyFn(() => {
    const module = "./QuickViewPlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
