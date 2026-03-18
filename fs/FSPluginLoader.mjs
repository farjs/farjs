import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(["M-l", "M-r", "M-h", "C-d"], async () => {
  const module = "./FSPlugin.mjs";
  return (await import(module)).default.instance;
});
