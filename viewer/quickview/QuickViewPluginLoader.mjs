import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(["C-q"], async () => {
  const module = "./QuickViewPlugin.mjs";
  return (await import(module)).default;
});
