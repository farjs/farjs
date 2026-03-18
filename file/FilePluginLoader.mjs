import FileListPluginLoader from "@farjs/filelist/FileListPluginLoader.mjs";

export default FileListPluginLoader(["M-v"], async () => {
  const module = "./FilePlugin.mjs";
  return (await import(module)).default;
});
