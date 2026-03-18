import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(["f5", "f6", "S-f5", "S-f6"], async () => {
  const module = "./CopyMovePlugin.mjs";
  return (await import(module)).default;
});
