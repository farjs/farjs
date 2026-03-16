import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default new FileListPluginLoader(
  ["S-f7"],
  lazyFn(() => {
    const module = "./ArchiverPlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
