import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(
  ["f1", "f7", "f8", "delete", "f9", "f10", "M-s", "M-d"],
  async () => {
    const module = "./FileListUiPlugin.mjs";
    return (await import(module)).default;
  },
);
