import { lazyFn } from "@farjs/filelist/utils.mjs";
import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";
import FileEvent from "../file/FileEvent.mjs";
import ViewerEvent from "./ViewerEvent.mjs";

export default new FileListPluginLoader(
  [
    "f3",
    ViewerEvent.onViewerOpenLeft,
    ViewerEvent.onViewerOpenRight,
    FileEvent.onFileView,
  ],
  lazyFn(() => {
    const module = "./ViewerPlugin.mjs";
    return import(module).then((_) => _.default);
  }),
);
