import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["M-l", "M-r", "M-h", "C-d"],
  lazyFn(() => {
    const module = "./FSPlugin.mjs";
    return import(module).then((_) => _.default.instance);
  }),
);
