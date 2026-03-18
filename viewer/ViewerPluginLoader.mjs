import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";
import FileEvent from "../file/FileEvent.mjs";
import ViewerEvent from "./ViewerEvent.mjs";

export default FileListPluginLoader(
  [
    "f3",
    ViewerEvent.onViewerOpenLeft,
    ViewerEvent.onViewerOpenRight,
    FileEvent.onFileView,
  ],
  async () => {
    const module = "./ViewerPlugin.mjs";
    return (await import(module)).default;
  },
);
